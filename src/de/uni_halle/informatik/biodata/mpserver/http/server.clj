(ns de.uni-halle.informatik.biodata.mpserver.http.server
  (:require
   [clojure.java.io :as io]
   [de.uni-halle.informatik.biodata.mpserver.http.handler :as handler]
   [de.uni-halle.informatik.biodata.mpserver.http.middleware :as middleware]
   [de.uni-halle.informatik.biodata.mpserver.wrapper.polishing :as polishing]
   [mount.core :refer [defstate] :as mount]
   [reitit.ring :as ring]
   [reitit.swagger-ui :as swagger-ui]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.json :refer [wrap-json-response]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.util.response :as response]
   [de.uni-halle.informatik.biodata.mpserver.config :refer [app-config]]))

(def app
  (ring/ring-handler
   (ring/router
    [""
     ["/submit"
      ["/file" {:post {:handler    handler/submit-handler
                       :middleware [wrap-json-response
                                    middleware/wrap-with-context
                                    middleware/wrap-with-diff
                                    middleware/wrap-with-save-file!]}}]]
     ;; this exposes the OpenAPI spec ..
     ["/openapi.json"
      {:get {:handler (fn [_]
                        (-> (io/resource "openapi.json")
                            (io/input-stream)
                            (response/response)))}}]
     ;; ... which is used by the swagger UI (unfortunately with full path at the moment)
     ["/docs/*" {:no-doc true
                 :get    (swagger-ui/create-swagger-ui-handler
                          {:url (str (:subpath app-config) "/openapi.json")})}]]
    {:data     {:middleware [middleware/wrap-mdc!
                             middleware/wrap-exception
                             wrap-params
                             wrap-multipart-params]}})
   (ring/routes ;; combine two handlers
      (ring/redirect-trailing-slash-handler)
      (ring/create-default-handler
       {:not-acceptable (constantly {:status 406, :body ""})}))))

#_(.stop jetty)
#_(def jetty
    (-> (wrap-reload #'app)
        #_app
        (run-jetty {:port 3000
                    :join? false
                    :send-server-version? false})))

;; see core namespace: this is some magic component framework stuff
;; which serves to start up system components/objects on startup
(defstate http-server
  :start (let [{:keys [port subpath]} app-config]
           (-> app
               (run-jetty {:port (int port)
                           ;; otherwise this would be blocking
                           :join? false
                           ;; otherwise our server goes advertising what kind of server he is and why should he
                           :send-server-version? false})))
  :stop (.stop http-server))

;; (mount/start #'http-server)
;; (mount/stop #'http-server)
