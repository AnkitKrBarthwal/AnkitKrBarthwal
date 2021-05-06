import java.util

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.json.{JsBoolean, JsNull, JsNumber, JsObject, JsString, JsValue, Json, Writes}
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

import scala.annotation.tailrec

/*
//required
com.amazonaws.services.dynamodbv2.model.AttributeValue

//found
software.amazon.awssdk.services.dynamodb.model.AttributeValue*/

import software.amazon.awssdk.services.dynamodb.model._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


object Initiator extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val tableName: String = "test_03_05_2021_a"

  val ops: SpecialOps = new SpecialOps(tableName)

  lazy val keySchemaElements: List[KeySchemaElement] = List(
    //https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-partition-key-uniform-load.html
    KeySchemaElement.builder().attributeName("email").keyType(KeyType.HASH).build(),
    KeySchemaElement.builder().attributeName("name").keyType(KeyType.RANGE).build()
  )

  lazy val attributeDefinitions: List[AttributeDefinition] = List(
    AttributeDefinition.builder().
      attributeName("email")
      .attributeType("S").build(),

    AttributeDefinition.builder().
      attributeName("name")
      .attributeType("S").build()
  )

  lazy val provisionedThroughput: ProvisionedThroughput =
    ProvisionedThroughput.builder().
      readCapacityUnits(20L).
      writeCapacityUnits(20L).
      build()

  //===================================== create table =================================

  ops.createTable(keySchemaElements, attributeDefinitions, provisionedThroughput).onComplete {
    case Success(createTableResponse: CreateTableResponse) =>
      println(createTableResponse)
    case Failure(exception) =>
      exception match {
        case _: java.util.concurrent.CompletionException => println(s"\n\nTable $tableName already exist")
        case tt => exception.printStackTrace()
      }
  }

  Thread.sleep(5000)

  //===================================== create =======================================

  val email = s"${System.currentTimeMillis()}@example.com"

  val keysData: Map[String, AttributeValue] = Map(
    "email" -> AttributeValue.builder().s(email).build(),
    "name" -> AttributeValue.builder().s("example").build())

  val jsonMap = Map("department" -> AttributeValue.builder().s("IT").build(), "empNo" -> AttributeValue.builder().n("10").build(), "isConfirmed" -> AttributeValue.builder().bool(true).build())

  val itemToAdd: Map[String, AttributeValue] = keysData ++ Map("userData" -> AttributeValue.builder().m(jsonMap.asJava).build())

  ops.putItem(itemToAdd).onComplete {
    case Success(_) =>
      ops.getItem(keysData).onComplete {
        case Success(getItemResponse: GetItemResponse) =>

          println(s"\n\nItem added Successfully! i.e " + JsonUtils.toJsonAsString(getItemResponse.item()))

        case Failure(exception) => exception.printStackTrace()
      }
    case Failure(exception) => exception.printStackTrace()
  }

  Thread.sleep(5000)

  //===================================== update =======================================
  ops.updateItem(keysData, """501""").onComplete {
    case Success(_) =>
      ops.getItem(keysData).onComplete {
        case Success(getItemResponse) => println("\n\nResult after update " + JsonUtils.toJsonAsString(getItemResponse.item()))
        case Failure(exception) =>
          println("Unable to read by update")
          exception.printStackTrace()
      }
    case Failure(exception) =>
      println("Unable to update")
      exception.printStackTrace()
  }

  Thread.sleep(5000)
  //======================================= Update particular===========================

  ops.updateItemParticularJsonField(keysData).onComplete {
    case Success(_) =>
      ops.getItem(keysData).onComplete {
        case Success(getItemResponse) => println("\n\nResult after Particular field update is " + JsonUtils.toJsonAsString(getItemResponse.item()))
        case Failure(exception) =>
          println("Unable to read by updateItemParticularJsonField")
          exception.printStackTrace()
      }
    case Failure(exception) =>
      println("Unable to updateItemParticularJsonField")
      exception.printStackTrace()
  }

  Thread.sleep(5000)
  //===================================== delete =======================================
  ops.deleteItem(keysData).onComplete {
    case Success(_) =>
      ops.getItem(keysData).onComplete {
        case Success(getItemResponse) => println("\n\nResult after delete " + JsonUtils.toJsonAsString(getItemResponse.item()))
        case Failure(exception) => exception.printStackTrace()
      }
    case Failure(exception) => println("Unable to delete")
      exception.printStackTrace()
  }
  scala.io.StdIn.readLine()
}

object JsonUtils {

  implicit val writes: Writes[Map[String, Any]] = new Writes[Map[String, Any]]{
    override def writes(o: Map[String, Any]): JsValue = {
      JsObject(
        o.map{kvp =>
          kvp._1 -> (kvp._2 match {
            case x: String => JsString(x)
            case x: Int => JsNumber(x)
            case x: Boolean => JsBoolean(x)
            case x: Map[String, Any] => writes(x)
            case _ => JsNull
          })
        }
      )
    }
  } 
  
  def toJsonAsString(item: util.Map[String, AttributeValue]): String = Json.stringify(Json.toJson(outer(item)))
  
  def outer(item: util.Map[String, AttributeValue]): Map[String, Any] = {
    @tailrec
    def inner(keys: List[String], res: Map[String, Any]): Map[String, Any] = {
      keys match {
        case head :: rest => inner(rest, res ++ Map(head -> getValue(item.get(head))))
        case Nil => res
      }
    }

    inner(item.keySet().asScala.toList, Map.empty[String, Any])
  }

  private def getValue(attributeValue: AttributeValue): Any = {
    if (attributeValue.s != null) attributeValue.s
    else if (attributeValue.n != null) attributeValue.n.toInt
    else if (attributeValue.bool() != null) attributeValue.bool()
    else if (attributeValue.hasM) outer(attributeValue.m)
    else "unidentified type"
  }
}

