(ns super-omnia.views
  (:require [re-frame.core :refer [dispatch
                                   subscribe]]
            [super-omnia.helpers :as helpers]))

(defn fixed-category-child [category kind name]
  (let [category-id (:id category)]
    [:a {:on-click #(dispatch [:select-category-item kind category-id])} name]
    )
  )

(defn category-item [{:keys [id name open? has-children? selected?]}]
  (let [icon (cond
               (not has-children?) ""
               open? "fa-minus-square-o"
               :else "fa-plus-square-o")]
    (list
     [:a
      [:i {:class (str "fa " icon " fa-2x")
           :aria-hidden "true"
           :on-click #(dispatch [:toggl-category id])}]
      [:span {:class (str "category-item "
                          (if selected? "selected-category-item"))
              :on-click #(dispatch [:change-tree-root id])}
       name
       ]]
     )
    ))

(defn breadcrumb-item [name id]
  [:li {:class "category-breadcrumb"}
   [:a { :on-click #(dispatch [:change-tree-root id]) } name]
   ]
  )

(defn category-breadcrumb []
  (let [root (subscribe [:tree-root])
        categories (subscribe [:all-categories])]
    (fn []
      [:ul {:class "breadcrumbs"}
       [breadcrumb-item "Inicio" 0]
       (for [{name :name id :id} (helpers/breadcrumb-list @categories @root)]
         [breadcrumb-item name id]
         )]
      )))

(defn items-list []
  (let [items (subscribe [:current-items])]
    (fn []
      [:div
       [:div {:class "row small-up-2 medium-up-3 large-up-6"}
        (for [{name :name icon :icon} @items]
          [:div {:class "columns"}
           [:div {:class "item-view text-center"}
            [:span {:class "item-label" }  name]
            [:img {:class "item-icon thumbnail" :src icon}]
            ]
           ]
          )
        ]
       ]
      )))

(defn item-filter [text kind selected]
  [:li {:class (if (= kind selected) "selected-category-item")}
   [:a {:on-click #(dispatch [:select-item-filter kind])} text]
   ]
  )

(defn item-filter-menu []
  (let [selected (subscribe [:selected-filter])]
    (fn []
      [:ul {:class "menu"}
       (item-filter "Elementos" :elements @selected)
       (item-filter "Ações" :actions @selected)
       (item-filter "Qualidades" :qualities @selected)
       ]
      )))

(defn category-list [root nested?]
  (let [categories (subscribe [:category-query root])]
    (fn []
      [:ul {:class (str "vertical menu " (if nested? "nested"))}
       (for [{:keys [id open?] :as category} @categories]
         ^{:key id}
         [:li (list
               (category-item category)
               (if open? [category-list id true]))]
         )]
      )))

(defn app []
  [:div {:class "main-content"}
   [:div {:class "row"}
    [:div {:class "small-8 columns small-centered"}
     [:div {:class "callout"}
     [category-breadcrumb]
      ]
     ]]
   [:div {:class "row"}
    [:div {:class "small-4 columns"}
     [:div {:class "tree-view callout"}
      [category-list 0 false]]]
    [:div {:class "small-8 columns"}
     [:div {:class "elements-view callout"}
      [:div {:class "items-toolbar row"}
       [:div {:class "columns small-6"}
        [item-filter-menu]
        ]
       [:div {:class "columns small-6"}
        [:button {:class "float-right button"} [:i {:class "fa fa-bars"}]]]
       ]
      [items-list]]
     ]]]
  )
