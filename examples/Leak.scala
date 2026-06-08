package examples

import dev.capslock.*
import dev.capslock.model.*
import dev.capslock.repository.*
import dev.capslock.repository.db.*

/** capture checking の異常系を示すための、わざとコンパイルに失敗するコード。
  *
  * このファイルは `src/main` / `src/test` のいずれにも含めていないため、`sbt compile`
  * や `sbt test` の対象にはならない。代わりに `scripts/verify-leak.sh` から実ソースと
  * 一緒にコンパイルされ、capture checking が型エラーで拒否することを確認する。
  *
  * 以下の 2 つの値は、いずれも Repository が返したスコープに縛られた capability 参照を
  * [[TenantScope.withTenant]] のスコープ外へ持ち出そうとするため、「outlives its scope」
  * という型エラーになる。`scope` は given として渡されるため、Repository のメソッドは
  * `scope` を明示せずに呼び出している。
  *
  *   - `leaked`: `userRepo.all` が返す `User`（`User^{scope}`）をそのまま外へ返そうとする。
  *   - `leakedEmail`: `userRepo.all.head.email`（`Email^{scope}`）を外へ返そうとする。
  */
object Leak {
  val db = Database.seeded()
  val userRepo = UserRepository(db)

  // (1) User 参照そのものをスコープ外へ持ち出そうとする。
  val leaked: Option[Seq[User]] =
    TenantScope.withTenant("tenant1.app.example.com") { userRepo.all }

  // (2) Email 参照そのものをスコープ外へ持ち出そうとする。
  //     User.email は scope を capture する（Email^{scope}）ため、同様に型エラーになる。
  //     （unsafeValue で String にすればスコープ外へ出せるが、ここでは Email 参照を出す。）
  val leakedEmail: Option[Email] =
    TenantScope.withTenant("tenant1.app.example.com") {
      userRepo.all.head.email
    }
}
