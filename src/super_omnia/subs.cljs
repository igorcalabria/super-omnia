(ns super-omnia.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [super-omnia.helpers :as helpers]
            [clojure.string :as str]))

(register-sub
 :category-query
 (fn [db [_ root]]
   (let [all-categories (reaction (:categories @db))
         open-categories (reaction (:open-categories @db))
         wanted-categories (reaction (helpers/child-categories @all-categories root))
         tree-root (reaction (:tree-root @db))]
     (reaction (->> @wanted-categories
                    (map (partial helpers/set-is-open @open-categories))
                    (map (partial helpers/set-has-children @all-categories))
                    (map (partial helpers/set-selected @tree-root))
                    ))
     )
   ))

(register-sub
 :current-items
 (fn [db _]
   (let [id (reaction (:tree-root @db))
         selected-filter (reaction (:selected @db))
         ids (reaction (helpers/category-items @id @selected-filter (:categories @db)))
         ]

     (reaction (helpers/items-from-ids (get @db @selected-filter) @ids))
     )))

(register-sub
 :resource-icons
 (fn [db _]
   (let [icons (reaction (vals (:resource-icons @db)))
         search (reaction (:icon-search-value @db))]
     (reaction (helpers/filter-by-name @icons @search))
     )
   ))

(register-sub
 :current-form
 (fn [db _]
   (reaction (:current-element-form @db))))

(register-sub
 :all-actions
 (fn [db _]
   (let [search (reaction (:icon-search-value @db))
         actions (reaction (vals (:actions @db)))]
     (reaction (helpers/filter-by-name @actions @search))
     )
   ))

(register-sub
 :all-qualities
 (fn [db _]
   (let [search (reaction (:icon-search-value @db))
         qualities (reaction (vals (:qualities @db)))]
     (reaction (helpers/filter-by-name @qualities @search))
     )
   ))

(register-sub
 :selected-icon
 (fn [db _]
   (let [current-form (reaction (:current-element-form @db))
         selected (reaction (get-in @db [:forms @current-form :selected-icon]))]
     (reaction @selected))
   ))

(register-sub
 :form/item-name
 (fn [db _]
   (let [current-form (reaction (:current-element-form @db))]
     (reaction (get-in @db [:forms @current-form :item-name]))
     )))

(register-sub
 :action-menu
 (fn [db _]
   (reaction (:action-menu-open? @db))
   ))

(register-sub
 :action-modal
 (fn [db _]
   (reaction (:action-modal-open? @db))
   ))

(register-sub
 :selected-filter
 (fn [db _]
   (reaction (:selected @db))
   ))

(register-sub
 :tree-root
 (fn [db _]
   (reaction (get @db :tree-root))))

(register-sub
 :all-categories
 (fn [db _]
   (reaction (get @db :categories))))
