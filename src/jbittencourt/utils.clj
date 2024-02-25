(ns jbittencourt.utils)

(defn generate-random-byte-array [size]
  (let [byte-array (byte-array size)]
    (.nextBytes (new java.security.SecureRandom) byte-array)
    byte-array))

(defn parse-date-time [date-str]
  (let [formatter (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss")
        local-date-time (java.time.LocalDateTime/parse date-str formatter)
        instant (.atZone local-date-time (java.time.ZoneId/of "UTC"))]
    instant))

(defn date-time-str->timestamp [date-time-str]
  (-> date-time-str
      (parse-date-time)
      (.toEpochSecond)))

