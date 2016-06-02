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
     [:a
      [:i {:class (str "fa " icon " fa-2x")
           :aria-hidden "true"
           :on-click #(dispatch [:toggl-category id])}]
      [:span {:class (str "category-item "
                          (if selected? "selected-category-item"))
              :on-click #(dispatch [:change-tree-root id])}
       name
       ]]
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
        (for [{:keys [:name :icon :id]} @items]
          ^{:key id}
          [:div {:class "columns"}
           [:div {:class "item-view text-center"}
            [:label {:class "item-label" } name]
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
         [:li (category-item category) (if open? [category-list id true])]
         )]
      )))

(defn action-menu-item [text]
  [:a {:on-click #(dispatch [:toggl-action-modal])} text]
  )

(defn actions-menu []
  (let [open? (subscribe [:action-menu])]
    (fn []
      [:div
       [:button {:class "float-right button"
                 :on-click #(dispatch [:toggl-action-menu]) }
        [:i {:class "fa fa-bars"}]]
       [:div {:class (str (if @open? "is-open ")  "dropdown-pane")
              :style {:top "60px" :right "15px"}}
        [:ul {:class "vertical menu"}
         [:li (action-menu-item "Adicionar Categoria")]
         [:li (action-menu-item "Adicionar Elemento")]
         [:li (action-menu-item "Adicionar Qualidade")]
         [:li (action-menu-item "Adicionar Ação")]
         ]
        ]
       ]
      )
    )
  )

(defn icon-list [icons]
  [:div  {:class "row small-up-2 medium-up-4"}
   (for [{:keys [:id :icon :name] :as elem} icons]
     ^{:key id}
     [:div {:class "columns"}
      [:div {:class "item-view text-center" :on-click #(dispatch [:select-icon elem])}
       [:label {:class "item-label"} name]
       [:img {:class "item-icon thumbnail" :src icon}]
       ]
      ]
     )
   ]
  )

(defn icon-chooser []
  (fn []
    (let [resource-icons (subscribe [:resource-icons])]
      (println "heavy")
      [:div
       [:div {:class "row"}
        [:div {:class "small-6 columns"}
         [:div {:class "input-group"}
          [:span {:class "input-group-label"} [:i {:class "fa fa-search"}]]
          [:input {:class "input-group-field"
                   :type "text"
                   :placeholder "Escolha um Ícone"
                   :on-change #(dispatch [:icon-search (-> % .-target .-value)])}]
          ]
         ]
        ]
       [:div {:class "row"}
        [:div {:class "columns small-12"}
         [:div {:class "callout icon-chooser"}
          (icon-list (doall @resource-icons))
          ]
         ]
        ]
       ])))

(defn selected-icon-input []
  (fn []
    (let [selected-icon (subscribe [:selected-icon])]
      [:div {:class "columns small-7"}
       [:label "Ícone Escolhido"
        [:input {:type "text" :disabled true :value (:name @selected-icon)}]]
       ]
      )))

(defn item-name-input []
  (fn []
    (let [name (subscribe [:form/item-name])]
      [:div {:class "columns small-7"}
       [:label "Nome"
        [:input {:type "text"
                 :value @name
                 :on-change #(dispatch [:form/item-name (-> % .-target .-value)])}]
        ]
       [:p {:class "help-text"}
        "Você pode escolher outro nome para a categoria"
        ]
       ]
      )))

(defn modal-content []
  (fn []
    (let [params (subscribe [:form-params])]
      [:div
       [:h5 {:class "text-center"} "Nova Categoria"]
       [:form
        [:div {:class "row"} [icon-chooser]]
        [:div {:class "row"}
         [selected-icon-input]]
        [:div {:class "row"}
         [item-name-input]]
        [:div {:class "row"}
         [:div {:class "columns small-2 float-right"}
          [:button {:class "button success" :type "button"
                    :on-click #(dispatch [:create-category @params])} "Salvar"]
          ]]
        ]]
      )))

(defn action-modal []
  (let [open? (subscribe [:action-modal])]
    (fn []
      (let [display (if @open? "block" "none")]
        [:div
         [:div {:class "reveal-overlay"
                :id "overlay"
                :style {:display display}
                :on-click #(dispatch [:toggl-action-modal])}
          [:div {:class "reveal"
                 :id "content"
                 :on-click #(.stopPropagation %)
                 :style {:display display }}
           [:button {:class "close-button"
                     :on-click #(dispatch [:toggl-action-modal])
                     :type "button"}
            [:span {:aria-hidden true} "x"]]
           [modal-content]]
          ]]
        ))))

(defn app []
  [:div {:class "main-content"}
   [action-modal]
   [:div {:class "row"}
    [:div {:class "small-8 columns small-centered"}
     [:div {:class "callout"}
      [category-breadcrumb]]
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
        [actions-menu]
        ]
       ]
      [items-list]]
     ]]])
