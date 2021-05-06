import akka.stream.ActorMaterializer
import akka.stream.alpakka.dynamodb.scaladsl.DynamoDb
import software.amazon.awssdk.services.dynamodb.model._
import TTYL._

import scala.collection.JavaConverters._
import scala.concurrent.Future

class SpecialOps(tableName: String)(implicit val materializer: ActorMaterializer) {
  
  def createTable(
                   keySchemaElements: Seq[KeySchemaElement],
                   attributeDefinitions: Seq[AttributeDefinition],
                   provisionedThroughput: ProvisionedThroughput
                 ): Future[CreateTableResponse] = {
    DynamoDb.single(
      CreateTableRequest.builder()
        .tableName(tableName)
        .keySchema(keySchemaElements.asJava)
        .attributeDefinitions(attributeDefinitions.asJava)
        .provisionedThroughput(provisionedThroughput).build()
    )
  }

  def getItem(keyToGet: Map[String, AttributeValue]): Future[GetItemResponse] = {
    DynamoDb.single(
      GetItemRequest.builder()
        .tableName(tableName)
        .key(keyToGet.asJava)
        .build()
    )
  }
  
  def putItem(itemToPut: Map[String, AttributeValue]): Future[PutItemResponse] = {
    DynamoDb.single(
      PutItemRequest.builder()
        .tableName(tableName)
        .item(itemToPut.asJava)
        .build()
    )
  }

  def updateItem(whereAttribute: Map[String, AttributeValue], value: String): Future[UpdateItemResponse] = {
    DynamoDb.single(
      UpdateItemRequest.builder()
        .tableName(tableName)
        .key(Map("email" -> whereAttribute.getOrElse("email", AttributeValue.builder().s("").build()),
          "name" -> whereAttribute.getOrElse("name", AttributeValue.builder().s("").build())).asJava)
        .attributeUpdates(Map("userId" -> AttributeValueUpdate.builder().value(AttributeValue.builder().s(value).build()).build()).asJava)
        .build()
    )
  }

  def updateItemParticularJsonField(whereAttribute: Map[String, AttributeValue]): Future[UpdateItemResponse] = {
    DynamoDb.single(
      UpdateItemRequest.builder()
        .tableName(tableName)
        .key(Map("email" -> whereAttribute.getOrElse("email", AttributeValue.builder().s("").build()),
          "name" -> whereAttribute.getOrElse("name", AttributeValue.builder().s("").build())).asJava)
        .updateExpression("SET userData.department = :department")
        .expressionAttributeValues(Map(":department" -> AttributeValue.builder().s("HR").build()).asJava)
        .build()
    )
  }

  def deleteItem(whereAttribute: Map[String, AttributeValue]): Future[DeleteItemResponse] = {
    DynamoDb.single(
      DeleteItemRequest.builder()
        .tableName(tableName)
        .key(Map("email" -> whereAttribute.getOrElse("email", AttributeValue.builder().s("").build()),
          "name" -> whereAttribute.getOrElse("name", AttributeValue.builder().s("").build())).asJava)
        .build()
    )
  }

}
