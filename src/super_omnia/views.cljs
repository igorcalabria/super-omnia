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
               open? "fa-minus"
               :else "fa-plus")]
     [:a

      [:span {:class (str "category-item "
                          (if selected? "selected-category-item"))
              :on-click #(dispatch [:change-tree-root id])}
       name
       ]
      [:i {:class (str "fa " icon " category-expander")
           :aria-hidden "true"
           :on-click #(dispatch [:toggl-category id])}]
      ]
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
       (for [{name :name id :id} (helpers/breadcrumb-list @categories @root)]
         [breadcrumb-item name id]
         )]
      )))

(defn element-display [{:keys [:name :icon :id]}]
  [:div {:class "item-view text-center"}
   [:label {:class "item-label" } name]
   [:img {:on-click #(dispatch [:open-edit-form-modal :element id])
          :class "item-icon thumbnail"
          :src icon}]])

(defn category-display [{:keys [:name :icon :id]}]
  [:div {:class "item-view text-center"}
   [:label {:class "item-label"
            :on-click #(dispatch [:change-tree-root id])}
    [:a [:i.fa.fa-folder.icon-padding] name]]
   [:img {:on-click #(dispatch [:open-edit-form-modal :category id])
          :class "item-icon thumbnail"
          :src icon}]])

(defn items-list []
  (let [elements (subscribe [:current-elements])
        sub-categories (subscribe [:current-sub-categories])]
    (fn []
      [:div
       [:div.row.small-up-2.medium-up-3.large-up-6
        (for [{:keys [:name :icon :id] :as category} @sub-categories]
          [:div.columns [category-display category]])
        (for [{:keys [:name :icon :id] :as element} @elements]
          ^{:key id}
          [:div.columns [element-display element]])]])))

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

(defn action-menu-item [text kind]
  [:a {:on-click #(dispatch [:open-form-modal kind])} text]
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
         [:li (action-menu-item "Adicionar Categoria" :category)]
         [:li (action-menu-item "Adicionar Elemento" :element)]
         [:li (action-menu-item "Associar Ação" :assoc-action)]
         [:li (action-menu-item "Associar Qualidade" :assoc-quality)]
         ]
        ]
       ]
      )))

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
  (let [resource-icons (subscribe [:form-icons])]
    (fn []
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
          [icon-list (doall @resource-icons)]
          ]
         ]
        ]
       ]
      )))

(defn selected-icon-input []
  (let [selected-icon (subscribe [:selected-icon])]
    (fn []
      [:div {:class "columns small-7"}
       [:label "Ícone Escolhido"
        [:input {:type "text" :disabled true :value (:name @selected-icon)}]]
       ]
      )))

(defn item-name-input []
  (let [name (subscribe [:form/item-name])]
    (fn []
      [:div {:class "columns small-7"}
       [:label "Legenda"
        [:input {:type "text"
                 :value @name
                 :on-change #(dispatch [:form/item-name (-> % .-target .-value)])}]
        ]
       [:p {:class "help-text"}
        "Você pode escolher outra legenda"
        ]
       ]
      )))

(defn assoc-sugestion-form []
  (let [current-form (subscribe [:current-form])]
    (fn []
      (let [form-name (if (= @current-form :assoc-action) "Ação" "Qualidade")
            icon-source (if (= @current-form :assoc-action) :all-actions :all-qualities)]
        [:div
         [:h5.text-center (str "Associar Nova " form-name)]
         [:form
          [:div.row [icon-chooser icon-source]]
          [:div.row [selected-icon-input]]
          [:div.row
           [:div.columns.small-2.float-right
            [:button.button.success {:type "button"
                                     :on-click #(dispatch [:assoc-sugestion])} "Salvar"]
            ]
           ]
          ]
         ]
        ))))

(defn item-form []
  (let [current-form (subscribe [:current-form])
        action (subscribe [:form-action])
        action-name (if (= @action :edit) "Editar" "Criar")
        dispatch-action (if (= @action :edit) :edit-item :create-item)
        form-name (cond (= @current-form :category) "Categoria"
                        (= @current-form :element) "Elemento")]
    [:div
     [:h5 {:class "text-center"} (str action-name " " form-name)]
     [:form
      [:div {:class "row"} [icon-chooser]]
      [:div {:class "row"} [selected-icon-input]]
      [:div {:class "row"}
       [item-name-input]]
      [:div {:class "row"}
       [:div {:class "columns small-2 float-right"}
        [:button {:class "button success" :type "button"
                  :on-click #(dispatch [dispatch-action])} "Salvar"]]]]]))

(defn modal-content []
  (let [current-form (subscribe [:current-form])]
    (fn []
      (let [item-form? (contains? #{:element :category} @current-form)
            assoc-form? (contains? #{:assoc-action :assoc-quality} @current-form)]
        (cond
          item-form? [item-form]
          assoc-form? [assoc-sugestion-form]
          ))
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
    [:div {:class "small-4 columns"}
     [:div {:class "tree-view callout"}
      [category-list nil false]]]
    [:div {:class "small-8 columns"}
     [:div {:class "row"}
      [:div {:class "callout"}
       [category-breadcrumb]]
      ]
     [:div {:class "row"}
      [:div {:class "elements-view callout"}
       [:div {:class "items-toolbar row"}
        [:div {:class "columns small-6"}
         [item-filter-menu]]
        [:div {:class "columns small-6"}
         [actions-menu]
         ]
        ]
       [items-list]]
      ]]]])
