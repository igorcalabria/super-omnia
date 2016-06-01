(ns super-omnia.handlers
  (:require [re-frame.core :refer [register-handler
                                   dispatch]]
            [reagent.core :refer [atom]]
            [super-omnia.helpers :as helpers]
            [ajax.core :refer [GET POST]]))

(def api-root-url "http://localhost:8080/adesign/api/project/1")
(def initial-state {:open-categories #{}
                    :tree-root 0
                    :selected :elements
                    :action-menu-open? false
                    :action-modal-open? false })

(register-handler
 :initialise-db
 (fn [_ _]
   (GET api-root-url {:handler #(dispatch [:process-root-request %1])
                      :response-format :json
                      :keywords? true})
   initial-state))

(register-handler
 :create-category
 (fn [app-state [_ params]]
   (POST (str api-root-url "/category") {:params params
                                         :handler #(dispatch [:process-new-category %1])
                                         :response-format :json
                                         :keywords? true})
   app-state
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
 :toggl-action-modal
 (fn [app-state _]
   (let [open? (:action-modal-open? app-state)]
     (assoc app-state :action-modal-open? (not open?))
     )))

(register-handler
 :process-new-category
 (fn [app-state [_ response]]
   (let [category (helpers/parse-remote-category response)]
     (assoc-in app-state [:categories (:id category)] category)
     )))

(register-handler
 :process-root-request
 (fn [app-state [_ response]]
   (-> app-state
       (assoc :actions (helpers/parse-remote-item response :actions))
       (assoc :qualities (helpers/parse-remote-item response :qualities))
       (assoc :categories (helpers/parse-remote-categories response))
       (assoc :elements (helpers/parse-remote-elements response))
       )))
