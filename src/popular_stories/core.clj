(ns popular-stories.core
  (:require [popular-stories.handler :as handler])
  (:require [ring.adapter.jetty :as jetty]))

(defn -main [port]
  (jetty/run-jetty handler/app {:port port}))
