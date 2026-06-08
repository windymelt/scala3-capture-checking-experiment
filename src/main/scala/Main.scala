import dev.capslock.*
import dev.capslock.model.*
import dev.capslock.repository.*
import dev.capslock.repository.db.*

/** マルチテナント構成での capture checking デモ。
  *
  * 全テナントのデータが同居する単一の [[dev.capslock.repository.db.Database]] に対し、
  * [[dev.capslock.TenantScope.withTenant]] でテナントごとのスコープを開く。 DB アクセスは
  * [[dev.capslock.repository.UserRepository]] /
  * [[dev.capslock.repository.TenantRepository]] が担い、各メソッドは given の `scope`
  * を要求して `scope.tenantId` に限定したアクセスのみを行う。スコープ内で得た値をスコープの外へ 持ち出すと capture
  * checking が拒否する。
  */
@main def hello(): Unit = {
  val db = Database.seeded()
  val userRepo = UserRepository(db)
  val tenantRepo = TenantRepository(db)

  // tenant1 のスコープを開き、ユーザー名を集計して返す。
  // User そのものではなく String の Seq を返すため、安全に外へ取り出せる。
  val tenant1Names: Option[Seq[String]] =
    TenantScope.withTenant("tenant1.app.example.com") {
      userRepo.all.map(_.name)
    }
  println(s"tenant1 のユーザー: ${tenant1Names.getOrElse(Seq.empty).mkString(", ")}")

  // 別テナントのスコープでは、そのテナント自身の情報だけが見える。
  val tenant3Name: Option[String] =
    TenantScope.withTenant("tenant3.app.example.com") {
      tenantRepo.current.map(_.name).getOrElse("不明")
    }
  println(s"tenant3 のテナント名: ${tenant3Name.getOrElse("(解決できず)")}")

  // tenant1 のスコープでユーザーを追加する。tenantId は scope から強制付与されるため、
  // 他テナントのユーザーとして書き込むことはできない。
  TenantScope.withTenant("tenant1.app.example.com") {
    userRepo.insert("Carol of Tenant 1", "carol@tenant1.example.com")
  }
  val afterInsert: Option[Int] =
    TenantScope.withTenant("tenant1.app.example.com") {
      userRepo.all.size
    }
  println(s"追加後の tenant1 のユーザー数: ${afterInsert.getOrElse(0)}")

  // 未知のホストはテナントを解決できず None になる。
  val unknown: Option[Int] =
    TenantScope.withTenant("tenant9.app.example.com") {
      userRepo.all.size
    }
  println(s"tenant9（存在しない）: $unknown")
}

// 次のコードは capture checking によりコンパイルできない。
// Repository が返した User をスコープの外へ持ち出そうとするため。
//
//   val leaked: Option[Seq[User]] =
//     TenantScope.withTenant("tenant1.app.example.com") { userRepo.all }
