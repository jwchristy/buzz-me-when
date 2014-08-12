(ns buzz-me-when.accessor
  (:require [cheshire.core :refer :all]
            [clj-time.core :as time]))

(def alerts (atom {}))

(defn get-alert
  "Return an alert from the alerts map given a symbol"
  [symbol]
  (get (deref alerts) symbol))

(defn get-all-alerts
  "Return all of the alerts as a vector"
  []
  (into [] (vals (deref alerts))))

(defn- assoc-alert
  "Put an element to the alerts map with the symbol as the key"
  [previous-alerts alert]
  (assoc previous-alerts (alert :symbol) alert))

(defn put-alert-incessantly-as-directed
  "Add an alert to the system, recursively retrying on failure if directed"
  [keep-trying alert]
  (let [existing-alerts (deref alerts)]
    (if (compare-and-set! alerts existing-alerts (assoc-alert existing-alerts alert))
      true
      (if (keep-trying)
        (put-alert-incessantly-as-directed alert false)  ; Limit recursion to one level for now
        false))))

(def put-alert (partial put-alert-incessantly-as-directed true))

(defn put-alert-doc
  "Add an alert represented as a json document to the system"
  [alert-doc]
  (put-alert (parse-string alert-doc true)))

(defn mark-fulfilled
  "Mark an alert as fulfilled"
  [symbol]
  (let [alert (get-alert symbol)]
    (put-alert (assoc alert :fulfilled_date (str (time/now))))))

