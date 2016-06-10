(ns super-omnia.adesign-api
  (:require [ajax.core :refer [GET POST]]))

(def api-root "http://localhost:8080/adesign/api")

(defn- resource-url
  ([kind] (resource-url kind 0 0))
  ([kind project] (resource-url kind project 0))
  ([kind project category]
   (cond
     (= kind :category) (str api-root "/project/" project "/category")
     (= kind :icons) (str api-root "/resource/find/_")
     (= kind :root) (str api-root "/project/" project)
     (= kind :element) (str api-root "/category/" category "/element")
     ))
  )

(defn- params-middleware [params kind category-id]
  (if (= kind :category) (assoc params :catId category-id) params)
  )

(defn create [project category kind {:keys [:success :error :params]}]
  (let [processed-params (params-middleware params kind category)]
    (POST (resource-url kind project) {:params processed-params
                                       :handler success
                                       :response-format :json
                                       :format :json
                                       :keywords? true})
    ))

(defn icons [{:keys [:success :error]}]
  (GET (resource-url :icons) {:handler success
                              :response-format :json
                              :keywords? true }))

(defn root [project {:keys [:success :error]}]
  (GET (resource-url :root project) {:handler success
                                     :response-format :json
                                     :keywords? true }))
