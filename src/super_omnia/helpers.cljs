(ns super-omnia.helpers
  (:require [clojure.set :refer [rename-keys]]
            [clojure.string :as str]))

(defn toggl-set [set value]
  (if (get set value) (disj set value) (conj set value)))

(defn child-categories [categories root]
  (filter #(= root (:root %)) (vals categories)))

(defn category-items [category-id kind categories]
  (get-in categories [category-id kind]))

(defn items-from-ids [items ids]
  (map #(get items %) ids))

(defn find-item [app-state kind id]
  (let [kind-map {:category :categories
                  :quality :qualities
                  :action :actions
                  :element :elements}
        look-up-kind (kind kind-map)]
    (get-in app-state [look-up-kind id])))

(defn breadcrumb-list [categories root]
  (loop [root-id root
         result '()]
    (if (= root-id nil)
      result
      (let [category (get categories root-id)
            name (:name category)
            id (:id category)]
        (recur (:root category) (conj result {:name name :id id}))))))

(defn set-is-open [open-categories {id :id :as category}]
  (assoc category :open? (contains? open-categories id)))

(defn set-has-children [categories {id :id :as category}]
  (assoc category :has-children? (not-empty (child-categories categories id))))

(defn set-selected [tree-root {id :id :as category}]
  (assoc category :selected? (= tree-root id)))

(defn parse-remote-item [project kind]
  (into
   {} (map (fn [kind] [(:id kind) kind]) (kind project))))

(defn hashfy-remote-list [items]
  (into
   {} (map (fn [item] [(:id item) item]) items)))

(defn idfy-items [items]
  (map (fn [item] (:id item)) items))

(defn idfy-category-elements [category]
  (let [elements (:elements category)]
    (assoc category :elements (idfy-items elements))))

(defn filter-by-name [coll name]
  (filter #(str/includes? (:name %) name) coll))

(defn replace-with-id [coll k]
  (assoc coll k (get-in coll [k :id])))

(defn remote-params [params root]
  (-> params
      (replace-with-id :selected-icon)
      (rename-keys {:selected-icon :resId :item-name :name})))

(defn assoc-remote-params [params kind]
  (-> params
      (replace-with-id :selected-icon)
      (select-keys [:selected-icon])
      (rename-keys {:selected-icon :id})))

(defn translate-remote-attributes [item]
  (let [translation {:catId :root :relActionsId :actions :relQualitiesId :qualities}]
    (rename-keys item translation)))

(defn parse-remote-icons [{content :content :as response}]
  (hashfy-remote-list content))

(defn parse-remote-category [category]
  (-> category
      translate-remote-attributes
      idfy-category-elements))

(defn add-new-category [app-state response]
  (let [category (parse-remote-category response)]
    (assoc-in app-state [:categories (:id category)] category)))

(defn add-new-element [app-state response category-id]
  (let [category-elements (get-in app-state [:categories category-id :elements])
        new-elements (conj category-elements (:id response))]
    (-> app-state
        (assoc-in [:categories category-id :elements] new-elements)
        (assoc-in [:elements (:id response)] response))))

(defn add-new-sugestion [app-state response category-id kind]
  (let [current-suggestions (get app-state kind)
        new-suggestions (hashfy-remote-list response)
        [new-id _] (last new-suggestions)
        category-suggestions (get-in app-state [:categories category-id kind])]
    (-> app-state
        (assoc-in [:categories category-id kind] (conj category-suggestions new-id))
        (assoc kind (merge current-suggestions new-suggestions)))))

(defn add-new-remote-item [app-state response {:keys [:kind :category-id]}]
  (cond
    (= kind :category) (add-new-category app-state response)
    (= kind :element) (add-new-element app-state response category-id)
    (= kind :assoc-action) (add-new-sugestion app-state response category-id :actions)
    (= kind :assoc-quality) (add-new-sugestion app-state response category-id :qualities)))

(defn add-root-category [actions qualities categories]
  (conj categories {:id 0 :name "Inicio" :actions actions :qualities qualities :root nil}))

(defn parse-remote-categories [project]
  (let [categories (:categories project)
        actions (map :id (:actions project))
        qualities (map :id (:qualities project))]
    (->> categories
         (map translate-remote-attributes)
         (map idfy-category-elements)
         (add-root-category actions qualities)
         hashfy-remote-list)))

(defn build-element-list [acc category]
  (merge acc (hashfy-remote-list (:elements category))))

(defn parse-remote-elements [project]
  (reduce build-element-list {} (:categories project)))
