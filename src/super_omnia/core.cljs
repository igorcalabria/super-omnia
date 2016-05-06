(ns super-omnia.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:categories { 1 {:id 1
                                            :name "Alimentos"
                                            :actions [1]
                                            :elements []
                                            :qualities [1]
                                            :categories [{:id 2
                                                          :name "Lanches"
                                                          :elements []
                                                          :actions []
                                                          :qualities []
                                                          :categories []}]}
                                       5 {:id 5
                                           :name "Bebidas"
                                           :actions [1]
                                           :elements [3 4 5]
                                           :qualities [1]
                                           :categories [{:id 6
                                                         :elements []
                                                         :actions []
                                                         :qualities []
                                                         :name "Refrigerantes"
                                                         :categories []}]}
                                       }

                          :elements {1 {:name "Alimento 1" :id 1 }
                                     2 {:name "Alimento 2" :id 2 }
                                     3 {:name "Bebida 1" :id 2}
                                     4 {:name "Bebida 2" :id 3}
                                     5 {:name "Bebida 3" :id 4}
                                     }

                          :qualities {1 {:name "Coisa" :id 1 } }

                          :actions {1 {:name "Coisar" :id 1 } }

                          :category-view {:breadcrumb []
                                          :opened #{1 5}
                                          :selected {:kind :actions
                                                     :id 1 }}})
  )

(defn visit-category [id]
  (swap! app-state assoc-in [:category-view :breadcrumb] (conj (get-in @app-state [:category-view :breadcrumb]) id)))

(defn pop-category []
  (swap! app-state assoc-in [:category-view :breadcrumb] (drop-last (get-in @app-state [:category-view :breadcrumb]))))

(defn root-category []
  (last (get-in @app-state [:category-view :breadcrumb])))


(defn thing-by-ids [kind ids]
  (vals (select-keys (get @app-state kind) ids))
  )

(defn thing-by-id [kind id]
  (get-in @app-state [kind id])
  )

(defn sub-thing [kind id]
  (let [category (get-in @app-state [:categories id])]
    (let [ids (get category kind)]
      (thing-by-ids kind ids)))
  )

(defn selected-group []
  (let [{kind :kind id :id} (get-in @app-state [:category-view :selected])]
    (sub-thing kind id)
    )
  )

(defn toggl-set [set value]
  (if (get set value) (disj set value) (conj set value))
  )

(defn toggl-category [category]
  (let [open-set (get-in @app-state [:category-view :opened])]
    (swap! app-state assoc-in [:category-view :opened] (toggl-set open-set (:id category)))
    ))

(defn toggl-selection [kind category-id]
  (swap! app-state assoc-in [:category-view :selected] {:kind kind :id category-id}))

(defn category-group [name kind category-id]
  [:a {:on-click #(toggl-selection kind category-id) :class "small button secondary"} name]
  )

(defn sub-category [data]
  [:div
   [:a (:name data)]]
  )

(defn category [data, open?]
  [:div {:class "category-block"}

   [:div {:on-click #(toggl-category data) :class "button-group"}
    [:a {:class "button" } (:name data)]
    ]

   (when open?
     [:div {:class "inner-tree-view"}
      [:ul
       [:li [category-group "Elementos" :elements (:id data)]]
       [:li [category-group "Qualidades" :qualities (:id data)]]
       [:li [category-group "Acoes" :actions (:id data)]]
       (for [c (:categories data)]
         [:li [sub-category c]])
       ]]
     )])

(defn category-open? [category]
  (contains? (get-in @app-state [:category-view :opened]) (:id category))
  )

(defn current-category []
  (let [current-selection (get-in @app-state [:category-view :selected])]
    (get-in @app-state [:categories current-selection])
    )
  )

(defn current-elements []
  (selected-group)
  )

(defn current-categories []
  (let [root (root-category)]
    (if root
      (:categories (get-in @app-state [:categories root]))
      (vals (:categories @app-state))
      )
    )
  )

(defn current-breadcrumb []
  (get-in @app-state [:category-view :breadcrumb]))

(defn breadcrumb [state]
  [:div { :class "category-navigation" }
   [:ul { :class "breadcrumbs" }
    [:li "Inicio"]
    (for [elem (state)]
      (list
       [:li (:name (thing-by-id :categories elem))]
       )
      )
    ]
   ]
  )

(defn elements-view [elements]
  [:div {:class "callout elements-view"}
   [:h4 "Elementos"]
   [:ul
    (for [e (elements)]
      [:li (:name e)]
      )]]
  )

(defn tree-view [categories]
  [:div { :class "callout tree-view" }
   [:h4 "Categorias"]
   [breadcrumb #(current-breadcrumb)]
   [:ul
    (for [c (categories)]
      [:li {:class "tree-view-item"}[category c (category-open? c)]])]]
  )

(defn app []
  [:div { :class "main-content" }
   [:div { :class "row" }
    [:div { :class "small-12 small-centered columns"}
     [:div {:class "row"}
      [:div {:class "small-4 columns"}
       [tree-view #(current-categories)]
       ]
      [:div {:class "small-8 columns"}
       [elements-view #(current-elements)]
       ]
      ]
     ]
    ]
   ]
  )

(reagent/render-component [app]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
