(ns popular-stories.clients
  (:require [clj-http.client :as http]))

(def headers {"User-Agent" "pop-str-client"})

(def hn-keys [:title :url :author :points :num_comments])
(def reddit-keys [:title :url :author :ups :num_comments])

(defn get-hn-stories []
  ;; Returns all the front page stories on HN right now
  (let [response (http/get
                  "http://hn.algolia.com/api/v1/search?tags=front_page"
                  {:headers headers
                   :as :json})
        hits (get-in response [:body :hits])]
    (for [hit hits]
      (select-keys hit hn-keys))))

(defn get-reddit-stories []
  ;; Returns all the front page stories on reddit/r/programming
  (let [response (http/get
                  "http://reddit.com/r/programming.json"
                  {:headers headers
                   :as :json})
        children (map :data (get-in response [:body :data :children]))]
    (for [child children]
      (select-keys child reddit-keys))))
