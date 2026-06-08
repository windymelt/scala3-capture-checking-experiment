import dev.capslock.*
import dev.capslock.model.*
import dev.capslock.repository.*
import dev.capslock.repository.db.*

class TenantScopeSuite extends munit.FunSuite {

  test("既知のホストからテナントを解決し、そのテナントのユーザーのみ取得できる") {
    val db = Database.seeded()
    val userRepo = UserRepository(db)
    val names =
      TenantScope.withTenant("tenant1.app.example.com") {
        userRepo.all.map(_.name).sorted
      }
    assertEquals(
      names,
      Some(Seq("Alice of Tenant 1", "Bob of Tenant 1"))
    )
  }

  test("テナントごとに見えるユーザーが分離されている") {
    val db = Database.seeded()
    val userRepo = UserRepository(db)
    val emails =
      TenantScope.withTenant("tenant3.app.example.com") {
        // User.email は scope に束縛された Email。unsafeValue (String) のみ外へ返す。
        userRepo.all.map(user => user.email.unsafeValue).toSet
      }
    assertEquals(
      emails,
      Some(Set("alice@tenant3.example.com", "bob@tenant3.example.com"))
    )
  }

  test("TenantRepository は現在のスコープのテナントを返す") {
    val db = Database.seeded()
    val tenantRepo = TenantRepository(db)
    val name =
      TenantScope.withTenant("tenant2.app.example.com") {
        tenantRepo.current.map(_.name).getOrElse("")
      }
    assertEquals(name, Some("Tenant 2 Inc."))
  }

  test("insert で追加したユーザーは同一テナントのスコープから読める") {
    val db = Database.seeded()
    val userRepo = UserRepository(db)
    val names =
      TenantScope.withTenant("tenant4.app.example.com") {
        userRepo.insert("Carol of Tenant 4", "carol@tenant4.example.com")
        userRepo.all.map(_.name).sorted
      }
    assertEquals(
      names,
      Some(Seq("Alice of Tenant 4", "Bob of Tenant 4", "Carol of Tenant 4"))
    )
  }

  test("insert は他テナントのスコープからは見えない（テナント分離）") {
    val db = Database.seeded()
    val userRepo = UserRepository(db)
    // tenant4 に追加する。
    TenantScope.withTenant("tenant4.app.example.com") {
      userRepo.insert("Carol of Tenant 4", "carol@tenant4.example.com")
    }
    // tenant5 のスコープからは tenant4 の追加分は見えない。
    val tenant5Names =
      TenantScope.withTenant("tenant5.app.example.com") {
        userRepo.all.map(_.name).sorted
      }
    assertEquals(
      tenant5Names,
      Some(Seq("Alice of Tenant 5", "Bob of Tenant 5"))
    )
  }

  test("未知のホストはテナントを解決できず None になる") {
    val db = Database.seeded()
    val userRepo = UserRepository(db)
    val result =
      TenantScope.withTenant("tenant9.app.example.com") { userRepo.all.size }
    assertEquals(result, None)
  }

  test("ベースドメインが一致しないホストも None になる") {
    val db = Database.seeded()
    val userRepo = UserRepository(db)
    val result =
      TenantScope.withTenant("tenant1.example.com") { userRepo.all.size }
    assertEquals(result, None)
  }
}

  // 異常系（Repository が返した capability 参照をスコープ外へ持ち出すコードが
  // コンパイルできないこと）は、munit の `compileErrors` では検証できない。capture
  // checking の違反はコンパイラの CheckCaptures フェーズで報告される一方、
  // `compileErrors`（および `scala.compiletime.testing.typeChecks`）は typer フェーズ
  // までしか実行しないためである。異常系は実際にコンパイルを試みて失敗することを確認
  // する `scripts/verify-leak.sh`（対象は `examples/Leak.scala`）で検証する。
