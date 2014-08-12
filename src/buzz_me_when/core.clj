(ns buzz-me-when.core (:require [liberator.core :refer [resource defresource]]
                                [ring.middleware.params :refer [wrap-params]]
                                [ring.adapter.jetty :refer [run-jetty]]
                                [compojure.core :refer [defroutes ANY]]
                                [buzz-me-when.accessor :as accessor]
                                [buzz-me-when.monitor :as monitor]))

(defresource alert-retriever-marker [symbol]
  :allowed-methods [:get :put]
  :available-media-types ["application/json"]
  ; TODO: Validate user-provided symbol input before getting alert from store
  :exists? (fn [ctx] (if-let [alert (accessor/get-alert symbol)] {::alert alert}))
  :handle-ok ::alert
  ; TODO: Validate user-provided data before putting alert to store
  :put! (fn [ctx] (accessor/mark-fulfilled symbol)))

(defresource alert-poster-lister []
  :allowed-methods [:get :post]
  :available-media-types ["application/json"]
  :handle-ok (fn [ctx] (accessor/get-all-alerts))
  ; TODO: Validate user-provided data before putting alert to store
  :post! (fn [ctx] (accessor/put-alert-doc (slurp (get-in ctx [:request :body])))))

(defroutes alerts
  (ANY "/alert/:symbol" [symbol] (alert-retriever-marker symbol))
  (ANY "/alert" [] (alert-poster-lister)))

(def handler
  (-> alerts
    (wrap-params)))

(run-jetty #'handler {:port 3000})
