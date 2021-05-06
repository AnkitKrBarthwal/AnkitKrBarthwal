import java.net.URI

import Initiator.materializer.system
import com.github.matsluni.akkahttpspi.AkkaHttpClient
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

// see https://doc.akka.io/docs/alpakka/current/aws-shared-configuration.html

object TTYL {
  private val credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create("lw7tm", "gugyw5"))
  implicit val client: DynamoDbAsyncClient = DynamoDbAsyncClient
    .builder()
    .region(Region.AWS_GLOBAL)
    .credentialsProvider(credentialsProvider)
    .endpointOverride(URI.create("http://localhost:8000"))
    .httpClient(AkkaHttpClient.builder().withActorSystem(system).build())
    .build()

  system.registerOnTermination(client.close())
}
