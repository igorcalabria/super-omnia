(ns super-omnia.adesign-api
  (:require [ajax.core :refer [GET POST PUT]])
  (:require-macros [super-omnia.env :refer [cljs-env]]))

(def api-root (cljs-env :adesign-api-url))

(defn- resource-url
  ([kind] (resource-url kind 0 0))
  ([kind project] (resource-url kind project 0))
  ([kind project category]
   (cond
     (= kind :category) (str api-root "/project/" project "/category")
     (= kind :icons) (str api-root "/resource/find/_")
     (= kind :root) (str api-root "/project/" project)
     (= kind :element) (str api-root "/category/" category "/element")
     (= kind :edit-element) (str api-root "/element")
     (= kind :edit-category) (str api-root "/category")
     (= kind :edit-action) (str api-root "/action")
     (= kind :edit-quality) (str api-root "/quality")
     (= kind :assoc-action) (str api-root "/category/" category "/relatedAction")
     (= kind :assoc-quality) (str api-root "/category/" category "/relatedQuality")
     ))
  )

(defn- params-middleware [params kind category-id]
  (if (= kind :category) (assoc params :catId category-id) params)
  )

(defn update [kind {:keys [:success :error :params]}]
  (POST (resource-url kind) {:params params
                             :handler success
                             :response-format :json
                             :format :json
                             :keywords? true}))

(defn create [project category kind {:keys [:success :error :params]}]
  (let [processed-params (params-middleware params kind category)]
    (PUT (resource-url kind project category) {:params processed-params
                                                :handler success
                                                :response-format :json
                                                :format :json
                                                :keywords? true})
    ))

(defn icons [{:keys [:success :error]}]
  (GET (resource-url :icons) {:handler success
                              :params {:page 0 :size 200}
                              :response-format :json
                              :keywords? true }))

(defn root [project {:keys [:success :error]}]
  (GET (resource-url :root project) {:handler success
                                     :response-format :json
                                     :keywords? true }))
