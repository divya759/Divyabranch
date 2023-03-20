package pack
import cats.effect._
import cats.implicits._
import scala.concurrent.ExecutionContext

object obj {
  
case class Channel(name: String, id: Int, cost: Double, language: String)
case class Package(name: String, channels: List[Channel])
case class Plan(name: String, duration: Int, cost: Double)
case class Subscription(
  name: String,
  packageOpt: Option[Package],
  addOnPackageOpt: Option[Package],
  planOpt: Option[Plan]
)
}

trait SubscriptionService[F[_]] {
  def subscribe(subscription: Subscription): F[Unit]
  def getSubscription(name: String): F[Option[Subscription]]
}

class InMemorySubscriptionService[F[_]: Sync](
  implicit ec: ExecutionContext
) extends SubscriptionService[F] {
  private var subscriptions: Map[String, Subscription] = Map.empty

  def subscribe(subscription: Subscription): F[Unit] =
    Sync[F].delay {
      subscriptions += subscription.name -> subscription
    }

  def getSubscription(name: String): F[Option[Subscription]] =
    Sync[F].delay {
      subscriptions.get(name)
    }
}

object Main extends IOApp {
  implicit val ec: ExecutionContext = ExecutionContext.global

  val package1 = Package("Default", List(
    Channel("Channel 1", 1, 10.0, "English"),
    Channel("Channel 2", 2, 15.0, "Hindi")
  ))
  val package2 = Package("Sports", List(
    Channel("Sports Channel 1", 3, 5.0, "English"),
    Channel("Sports Channel 2", 4, 7.0, "Hindi")
  ))
  val plan1 = Plan("Monthly", 1, 20.0)
  val plan2 = Plan("Bi-Annual", 6, 100.0)
  val plan3 = Plan("Annual", 12, 200.0)

  val subscription1 = Subscription("Subscription 1", Some(package1), Some(package2), Some(plan1))
  val subscription2 = Subscription("Subscription 2", Some(package1), None, Some(plan3))

  def run(args: List[String]): IO[ExitCode] = {
    val subscriptionService = new InMemorySubscriptionService[IO]

    for {
      _ <- subscriptionService.subscribe(subscription1)
      _ <- subscriptionService.subscribe(subscription2)

      subscription1Opt <- subscriptionService.getSubscription("Subscription 1")
      _ <- IO(println(subscription1Opt))

      subscription2Opt <- subscriptionService.getSubscription("Subscription 2")
      _ <- IO(println(subscription2Opt))
    } yield ExitCode.Success
  }
}