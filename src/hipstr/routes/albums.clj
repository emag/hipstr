(ns hipstr.routes.albums
  (:require [compojure.core :refer :all]
            [hipstr.layout :as layout]
            [hipstr.models.album-model :as album]
            [hipstr.validators.album-validator :as v]
            [taoensso.timbre :as timbre]
            [noir.util.route :refer [restricted]]))

(defn render-recently-added-html
  "Simply renders the recently added page with the given context."
  [ctx]
  (layout/render "albums/recently-added.html" ctx))

(defn recently-added-page
  "Renders out the recently-added page."
  []
  (render-recently-added-html {:albums (album/get-recently-added)}))

(defn discographies-page
  "Renders out the discographies page."
  [artist]
  (layout/render "albums/discographies.html"
                 {:discographies (album/get-discographies-by-artist-name {:artist_name artist})
                  :artist artist}))

(defn recently-added-submit
  "Handles the add-album form on the recently-added page.
   In the case of validation errors or other unexpected errors,
   the :new key in the context will be set to the album
   information submitted by the user."
  [album]
  (let [errors (v/validate-new-album album)
        form-ctx (if (not-empty errors)
                   {:validation-errors errors :new album}
                   (try
                     (album/add-album! album)
                     {:new {} :success true}
                     (catch Exception e
                       (timbre/error e)
                       {:new album
                        :error "Oh snap! We lost the album. Try it again?"})))
        ctx (merge {:form form-ctx}
                   {:albums (album/get-recently-added)})]
    (render-recently-added-html ctx)))

(defroutes album-routes
           (GET "/albums/recently-added" [] (restricted (recently-added-page)))
           (GET "/albums/:artist" [artist] (discographies-page artist))
           (POST "/albums/recently-added" [& album-form]
             (restricted (recently-added-submit album-form))))
