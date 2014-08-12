(ns buzz-me-when.monitor
  (:require [clj-http.client :as client]
            [clojure-csv.core :as csv]
            [buzz-me-when.accessor :as accessor]
            [postal.core :as postal]
            [clojure.edn :as edn]))

(def config (edn/read-string (slurp "config.edn")))

(def finance-url-prefix "http://finance.yahoo.com/d/quotes.csv?s=")
(def finance-url-suffix "&f=sl1op&e=.csv")
(def server-conf {:host (:mail-host config) :user (:mail-login config) :ssl true})
(def text-domain (:text-domain config))
(def return-address (:mail-return-address config))
(def text-destination (:text-number config))
(def mail-password (:mail-password config))


(defn get-current-price
  "Get a security's current price"
  [symbol]
  (let [response (client/get (str finance-url-prefix symbol finance-url-suffix))]
    (if (= 200 (:status response))
      (Double/valueOf (nth (first (csv/parse-csv (:body response))) 1))
       0)))

(defn threshold-breached?
  "Determine if a stock's current price has crossed the alert price"
  [alert]
  (let [current-price (get-current-price (:symbol alert))
        alert-price (:price alert)]
    (if (= "L" (:direction alert))
      (> current-price alert-price)
      (< current-price alert-price))))

(defn send-buzz
  "Send a text message through the email gateway, returning symbol if successful"
  [text-number body password symbol]
  (let [result-map (postal/send-message (assoc server-conf :pass password)
                     {:from return-address
                      :to (str text-number text-domain)
                      :body body})]
    (prn (str "SB:" result-map))
    (if (= 0 (:code result-map))
      symbol
      nil)))

(defn monitor-alerts-indefinitely
  "Daemon function to watch for alert condition satisfied"
  []
  (while true
    (do
      (Thread/sleep 300000)       ; 5 minutes
      (if-let [alerts (accessor/get-all-alerts)]
        ; Find all non-fulfilled alerts, sending a text for and marking as
        ; fulfilled those whose current price breaches the alert-price
        (doall (->> alerts
                 (filter (fn [alert] (not (:fulfilled_date alert))))
                 (filter (fn [alert] (threshold-breached? alert)))
                 (map (fn [alert] (send-buzz text-destination
                                    (str "Alert - price threshold breached for " (:symbol alert))
                                    mail-password (:symbol alert))))
                 (filter identity)
                 (map (fn [result-symbol] (accessor/mark-fulfilled result-symbol))))))))
  nil)

; Kick off the monitor
(.start (Thread. monitor-alerts-indefinitely))
