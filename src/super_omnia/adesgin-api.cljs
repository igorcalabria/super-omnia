(ns super-omnia.adesign-api
  (:require [ajax.core :refer [GET POST]]))

;; TODO: Parametrize project id
(def api-root "http://localhost:8080/adesign/api/project/1")
(def resources-root-url "http://localhost:8080/adesign/api/resource/find/_")

(defn- resource-url [kind params]
  (cond
    (= kind :category) (str api-root "/category")
    ))

(defn create [kind {:keys [:success :error :params]}]
  (POST (resource-url kind params) {:params params
                                    :handler success
                                    :response-format :json
                                    :format :json
                                    :keywords? true}))

(defn icons [{:keys [:success :error]}]
  (GET resources-root-url {:handler success
                           :response-format :json
                           :keywords? true }))

(defn root [{:keys [:success :error]}]
  (GET api-root {:handler success
                     :response-format :json
                     :keywords? true }))
