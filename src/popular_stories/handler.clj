(ns popular-stories.handler
  (:require [popular-stories.clients :refer [get-hn-stories get-reddit-stories]])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [cheshire.core :as json]
            [ring.util.response :as r]))

(defonce rate-limiter (atom nil))

(defn within-rate-bounds? []
  (if (nil? @rate-limiter)
    true
    (let [now (System/currentTimeMillis)
          millis-diff (- now @rate-limiter)
          sec-diff (/ millis-diff 1000.0)]
      (> sec-diff 3))))

(defn intersection [request]
  (let [hn-stories (get-hn-stories)
        reddit-stories (get-reddit-stories)]
    {:status 200
     :body (filter
            (complement nil?)
            (for [h hn-stories]
              (some
               #(if (= (:url h) (:url %)) [h %])
               reddit-stories)))
     :headers {}}))

(defn wrap-rate-limit [handler]
  (fn [request]
    (if (within-rate-bounds?)
      (let [response (handler request)]
        (reset! rate-limiter (System/currentTimeMillis))
        response)
      (r/content-type (r/response "We cannot refresh too often!") "text/plain"))))

(defn wrap-prettify [handler]
  (fn [request]
    (let [response (handler request)
          json-response (json/generate-string (:body response) {:pretty true})]
      (->
       response
       (assoc :body json-response)
       (assoc-in [:headers "Content-Type"] "application/json")))))

(defroutes app-routes
  (GET "/intersection" [] (wrap-rate-limit
                           (wrap-prettify
                            intersection)))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
