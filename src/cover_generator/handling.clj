(ns cover-generator.handling
  (:require
    [tg-bot-api.telegram :as telegram]
    [blurhash.core :as blurhash]
    [blurhash.encode :as blurhash.encode]
    [blurhash.decode :as blurhash.decode]
    [fivetonine.collage.core :as collage]
    [fivetonine.collage.util :as collage.util]
    )
  (:import [java.awt Graphics2D Color Font]
           [java.awt.image BufferedImage]
           [javax.imageio ImageIO]
           [java.io File]))


(defn blur [in out]
  (let [blurred
        (-> (blurhash/file->pixels in)
            (blurhash.encode/encode)
            (blurhash.decode/decode 16 16)
            (blurhash/pixels->file out))]

    (collage.util/save
         (collage/resize
           (collage.util/load-image out)
           {:width  1080
            :height 1080})
         out)))


(defn save-file! [uri file]
  (with-open [in (clojure.java.io/input-stream uri)
              out (clojure.java.io/output-stream file)]
    (clojure.java.io/copy in out)))


(defn cover
  [uri]

  (let [offset 920

        blurred-path
        "target/blurred.jpg"

        cover-path
        "target/cover.jpg"

        cover
        (save-file! uri cover-path)

        blurred
        (blur cover-path blurred-path)

        w-logo
        (collage/paste
         (collage.util/load-image blurred-path)
         (collage.util/load-image "resources/logo.png")
         offset
         offset)]

    (collage.util/save w-logo "target/cover.jpg")))



(defn the-handler
  "Bot logic here"
  [config {:keys [message]} trigger-id]

  (let [{:keys [photo document]} message]
    (when photo
      (->> photo
           (sort-by :width)
           last
           :file_id
           (telegram/get-file config)
           :url
           cover
           clojure.java.io/file
           (telegram/send-document config (-> message :chat :id))
           ))))



#_(cover "/Users/m0x3mkx/Pictures/lmnd/ebalo_new_round-c7b7b2f5001bf1f68b244c939d855c375e8ff3ce3aa4b25edca1110894088cda.png")
#_(sort-by :a [{:a 1} {:a 3} {:a 2}])
#_(let [config {:token (clojure.string/trim-newline
                      (slurp "token"))}]
  (telegram/get-file
   config
   "AgACAgIAAxkBAAIEoWTRkcnrrw17alUwIh7c_N1P2OIxAAL_zDEbYReJSnNM3ZFF8uxVAQADAgADcwADMAQ"))
#_(telegram/send-document
 {:token (clojure.string/trim-newline (slurp "token"))}
 163440129
 (clojure.java.io/file "target/cover.jpg")
 )
