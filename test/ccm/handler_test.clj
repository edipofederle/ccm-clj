(ns ccm.handler-test
  (:use liberator.core
        midje.sweet

        [ring.mock.request :only [request header]]
        [ccm.checkers :refer :all]))
