(ns buzz-me-when.core (:require [liberator.core :refer [resource defresource]]
                                [liberator.representation :refer [ring-response]]
                                [ring.middleware.params :refer [wrap-params]]
                                [ring.adapter.jetty :refer [run-jetty]]
                                [compojure.core :refer [defroutes ANY]]
                                [buzz-me-when.accessor :as accessor]
                                [buzz-me-when.monitor :as monitor]))

(defresource alert-retriever-marker [symbol]
  :allowed-methods [:get :put :options]
  :available-media-types ["application/json"]
  ; TODO: Validate user-provided symbol input before getting alert from store
  :exists? (fn [ctx] (if-let [alert (accessor/get-alert symbol)] {::alert alert}))
  :handle-ok ::alert
  ; TODO: Validate user-provided data before putting alert to store
  :put! (fn [ctx] (accessor/mark-fulfilled symbol))
  :handle-options (fn [_] (do (prn "HERE HERE HERE")
                            (ring-response {:status 200 :body "{}"  :headers {"Access-Control-Allow-Origin" "*"}}))))
;                                            "Access-Control-Allow-Methods" "GET, POST, OPTIONS, PUT, PATCH, DELETE"
;                                            "Access-Control-Allow-Headers" "X-Requested-With,content-type"
;                                            "Access-Control-Allow-Credentials" true}})))
;  :as-response (fn [d ctx] (do (prn "Blah")
;                 (-> (as-response d ctx) ;; default implementation
;                   (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
;                   ))))

(defresource alert-poster-lister []
  :allowed-methods [:get :post :options]
  :available-media-types ["application/json"]
  :handle-ok (fn [ctx] (accessor/get-all-alerts))
  ; TODO: Validate user-provided data before putting alert to store
  :post! (fn [ctx] (accessor/put-alert-doc (slurp (get-in ctx [:request :body])))))

(defresource alert-monitor-starter []
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :post! (fn [ctx] (monitor/start-monitor)))

(defroutes alerts
  (ANY "/alert/:symbol" [symbol] (alert-retriever-marker symbol))
  (ANY "/alert" [] (alert-poster-lister))
  (ANY "/alert-monitor" [] (alert-monitor-starter)))

(def handler
  (-> alerts
    (wrap-params)))

(run-jetty #'handler {:port 3000})

