(ns user
  (:require [ring.adapter.jetty :as jetty]
            [compojure.core :refer [POST GET defroutes]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response]]
            [cheshire.core :refer [parse-string]]
            [ring.middleware.cors :refer [wrap-cors]]
            [clojure.set :refer [rename-keys]]))

(def project-json-file
  (slurp "resources/api/project.json"))

(def category-json-file
  (slurp "resources/api/category.json"))

(defn project-view []
  (response (parse-string project-json-file)))

(defn category-view []
  (assoc (response (parse-string category-json-file)) :status 201 ))

(defroutes app
  (GET "/adesign/api/project/:id" [] (project-view))
  (POST "/adesign/api/project/:id/category" [] (category-view))
  )

(def reloadable-app
  (-> app
      wrap-json-response
      wrap-reload
      ))

(def cors-app
  (wrap-cors reloadable-app :access-control-allow-origin [#"http://localhost:3449"]
                       :access-control-allow-methods [:get :put :post :delete]))

(defonce server (jetty/run-jetty #'cors-app {:port 8080 :join? false}))

(defn reload []
  (.stop server)
  (.start server)
  )
