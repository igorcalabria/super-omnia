(ns super-omnia.handlers
  (:require [re-frame.core :refer [register-handler
                                   dispatch]]
            [reagent.core :refer [atom]]
            [super-omnia.helpers :as helpers]
            [super-omnia.adesign-api :as api]))

(def initial-state {:open-categories #{}
                    :tree-root 0
                    :selected :elements
                    :action-menu-open? false
                    :action-modal-open? false
                    :icon-search-value ""})

(register-handler
 :initialise-db
 (fn [_ _]
   (api/icons {:success #(dispatch [:process-resource-icons %1])})
   (api/root 1 {:success #(dispatch [:process-root-request %1])})
   initial-state))

(register-handler
 :create-item
 (fn [app-state _]
   (let [current-form (:current-element-form app-state)
         params (get-in app-state [:forms current-form])
         tree-root (:tree-root app-state)
         remote-params (helpers/remote-params params tree-root)
         meta {:kind current-form :category-id tree-root}]

     (api/create 1 tree-root current-form {:success #(dispatch [:process-new-item %1 meta])
                                           :params remote-params}))
   app-state
   ))

(register-handler
 :icon-search
 (fn [app-state [_ search-value]]
   (assoc app-state :icon-search-value search-value)
   ))

(register-handler
 :select-icon
 (fn [app-state [_ {id :id item-name :name, :as selected}]]
   (let [current-form (:current-element-form app-state)]
     (-> app-state
         (assoc-in [:forms current-form :selected-icon] selected)
         (assoc-in [:forms current-form :item-name] item-name)
         ))
   ))

(register-handler
 :form/item-name
 (fn [app-state [_ name]]
   (let [current-form (:current-element-form app-state)]
     (assoc-in app-state [:forms current-form :item-name] name)
     )
   ))

(register-handler
 :change-tree-root
 (fn [app-state [_ root]]
   (assoc-in app-state [:tree-root] root)))

(register-handler
 :toggl-category
 (fn [app-state [_ category-id]]
   (let [open-categories (:open-categories app-state)]
     (assoc-in app-state [:open-categories] (helpers/toggl-set open-categories category-id))
     )))

(register-handler
 :select-item-filter
 (fn [app-state [_ kind]]
   (assoc app-state :selected kind)
   ))

(register-handler
 :toggl-action-menu
 (fn [app-state _]
   (let [open? (:action-menu-open? app-state)]
     (assoc app-state :action-menu-open? (not open?))
     )
   ))

(register-handler
 :open-form-modal
 (fn [app-state [_ kind]]
   (-> app-state
       (assoc :action-modal-open? true)
       (assoc :current-element-form kind))
   ))

(register-handler
 :toggl-action-modal
 (fn [app-state _]
   (let [open? (:action-modal-open? app-state)]
     (assoc app-state :action-modal-open? (not open?))
     )))

(register-handler
 :process-new-item
 (fn [app-state [_ response meta]]
   (-> app-state
       (helpers/add-new-remote-item response meta)
       (assoc :action-modal-open? false)
       (dissoc :selected-icon)
       (dissoc :form/item-name)
       )
   ))

(register-handler
 :process-resource-icons
 (fn [app-state [_ response]]
   (assoc app-state :resource-icons (helpers/parse-remote-icons response))
   ))

(register-handler
 :process-root-request
 (fn [app-state [_ response]]
   (-> app-state
       (assoc :actions (helpers/parse-remote-item response :actions))
       (assoc :qualities (helpers/parse-remote-item response :qualities))
       (assoc :categories (helpers/parse-remote-categories response))
       (assoc :elements (helpers/parse-remote-elements response))
       )))
