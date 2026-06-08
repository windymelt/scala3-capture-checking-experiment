package dev.capslock.model

import dev.capslock.TenantScope

/** メールアドレスを表す値オブジェクト（ドメインモデル）。
  *
  * 生成には [[TenantScope]] が必要で、渡された `scope` を capture する。したがって
  * `Email` の値は `Email^{scope}` となり、その scope
  * （[[dev.capslock.TenantScope.withTenant]] のブロック）の外へは持ち出せない。
  * `Email` を含む [[User]] も同様に `User^{scope}` となる。
  *
  * 永続化層（`Database`）が、保存している生の文字列から `using TenantScope` のもとで
  * この `Email` を構築する。
  *
  * なお `unsafeValue` で生の `String` を取り出した値は capture を持たないため、その
  * 文字列自体は外へ出せる。capture checking が縛るのは「`Email` 参照のライフタイム」で
  * あり、「`String` 値の伝播」ではないことに注意する。フィールド名に `unsafe` を冠して
  * いるのは、この取り出しがスコープによる縛りを外す操作であることを示すためである。
  */
final case class Email(unsafeValue: String)(using TenantScope^)
