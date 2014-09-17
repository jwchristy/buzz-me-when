(ns buzz-me-when.core (:require [liberator.core :refer [resource defresource]]
                                [ring.middleware.params :refer [wrap-params]]
                                [ring.adapter.jetty :refer [run-jetty]]
                                [compojure.core :refer [defroutes ANY]]
                                [ring.middleware.cors :refer [wrap-cors]]
                                [buzz-me-when.accessor :as accessor]
                                [buzz-me-when.monitor :as monitor]))

(defresource alert-reader-deleter [symbol]
  :allowed-methods [:get :delete :options]
  :available-media-types ["application/json"]
  ; TODO: Validate user-provided symbol input before getting alert from store
  :exists? (fn [_] (if-let [alert (accessor/get-alert symbol)] {::alert alert}))
  :handle-ok ::alert
  ; TODO: Validate user-provided data before removing alert from store
  :delete! (fn [_] (accessor/remove-alert symbol)))

(defresource alert-cluer []
  :allowed-methods [:get :post :put :options]
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (accessor/get-all-alerts))
  ; TODO: Validate user-provided data before putting alert to store
  :post! (fn [ctx] (accessor/put-alert-doc (slurp (get-in ctx [:request :body]))))
  :put! (fn [ctx] (accessor/put-alert-doc (slurp (get-in ctx [:request :body])))))

(defresource alert-monitor-starter []
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :post! (fn [_] (monitor/start-monitor)))

(defroutes alerts
  (ANY "/alert/:symbol" [symbol] (alert-reader-deleter symbol))
  (ANY "/alert" [] (alert-cluer))
  (ANY "/alert-monitor" [] (alert-monitor-starter)))

(def handler
  (-> alerts
    (wrap-params)
    (wrap-cors :access-control-allow-origin #"http://localhost:8000"
      :access-control-allow-methods [:get :post :put :delete])))

(run-jetty #'handler {:port 3000})

