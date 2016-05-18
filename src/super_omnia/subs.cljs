(ns super-omnia.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [super-omnia.helpers :as helpers]))

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
