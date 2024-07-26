(ns edu.tue.csb.mpserver.http.middleware)

(defn wrap-remove-x-id  [handler-fn]
  (fn [req]
    (let [resp (handler-fn req)]
      (update-in resp [:body] dissoc :x-id))))

(defn wrap-exception [handler]
  (fn [request]
    (try (handler request)
         (catch clojure.lang.ExceptionInfo e
           (prn e)
           {:status 500
            :body   "Exception caught"})
         (catch Exception e
           (prn e)
           {:status 500
            :body   "Exception caught"}))))
