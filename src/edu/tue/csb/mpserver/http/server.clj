(ns edu.tue.csb.mpserver.http.server
  (:require
   [clojure.java.io :as io]
   [edu.tue.csb.mpserver.http.handler :as handler]
   [edu.tue.csb.mpserver.http.middleware :as middleware]
   [edu.tue.csb.mpserver.wrapper.polishing :as polishing]
   [mount.core :refer [defstate]]
   [reitit.ring :as ring]
   [reitit.ring.spec :as rs]
   [reitit.swagger-ui :as swagger-ui]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.json :refer [wrap-json-response]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.util.response :as response]))

(def app
  (ring/ring-handler
   (ring/router
    ["/api"
     ["/submit"
      ["/file" {:post {:handler    handler/submit-handler
                       :middleware [wrap-params
                                    wrap-multipart-params
                                    wrap-json-response]}}]]
     
     ["/openapi.json"
      {:get {:no-doc  true
             :openapi {:info {:title       "Model Polisher API"
                              :description "API for the Model Polisher."
                              :version     polishing/model-polisher-version}}
             :handler (fn [_]
                           (response/response (io/input-stream
                                               (io/resource "openapi.json"))))}}]
     ["/docs/*" {:no-doc true
                 :get    (swagger-ui/create-swagger-ui-handler {:url "/api/openapi.json"})}]]
    {:data     {:middleware [middleware/wrap-exception]}
     :validate rs/validate})
   (ring/routes ;; combine two handlers
      (ring/redirect-trailing-slash-handler)
      (ring/create-default-handler
       ;; wird geworfen bei nil return value und fälschlich als 400er rausgegeben
       {:not-acceptable (constantly {:status 406, :body ""})}))))



#_(.stop jetty)
#_(def jetty
  (-> (wrap-reload #'app)
      #_app
      (run-jetty {:port 3000
                  :join? false})))

(defstate http-server
  :start (-> app
             (run-jetty {:port 3000
                         :join? false}))
  :stop (.stop http-server))

;; (mount/start #'http-server)
;; (mount/stop #'http-server)
