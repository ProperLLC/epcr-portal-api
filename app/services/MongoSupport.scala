package services

import play.api.Play.current

import play.modules.reactivemongo.ReactiveMongoPlugin

import scala.concurrent.ExecutionContext

/**
 * Created by terry on 1/2/14.
 *
 * Series of helpers for classes that need to use MongoDB
 */
trait MongoSupport {
  def driver = ReactiveMongoPlugin.driver
  def connection = ReactiveMongoPlugin.connection
  def db = ReactiveMongoPlugin.db

  implicit def ec : ExecutionContext = ExecutionContext.Implicits.global
}

