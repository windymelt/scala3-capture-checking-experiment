package dev.capslock.model

/** リクエストの URL ホスト部分からテナントを決定する。
  *
  * テナントはホスト名のサブドメインで識別される。固定ドメイン `app.example.com` の
  * 前に付くラベルがテナント ID となる。例えば `tenant1.app.example.com` であれば
  * テナント ID は `tenant1` である。
  *
  * 本サイトに存在するテナントは `tenant1` から `tenant5` までの 5 つに限られる。
  * それ以外のホストは未知のテナントとして解決に失敗する。
  */
object TenantResolver {

  /** テナント識別のベースとなる固定ドメイン。 */
  val baseDomain: String = "app.example.com"

  /** 本サイトに存在する有効なテナント ID の集合（tenant1〜tenant5）。 */
  val validTenantIds: Set[TenantId] =
    (1 to 5).map(n => TenantId(s"tenant$n")).toSet

  /** ホスト名からテナント ID を解決する。
    *
    * `<label>.app.example.com` の形式で、`<label>` が有効なテナント ID
    * （tenant1〜tenant5）に一致する場合のみ [[scala.Some]] を返す。
    *
    * @param host
    *   リクエストのホスト部分（例: `tenant1.app.example.com`）
    * @return
    *   解決できた場合はテナント ID、未知のホストの場合は [[scala.None]]
    */
  def resolve(host: String): Option[TenantId] = {
    val suffix = "." + baseDomain
    if host.endsWith(suffix) then {
      val label = host.dropRight(suffix.length)
      val candidate = TenantId(label)
      Option.when(validTenantIds.contains(candidate))(candidate)
    }
    else None
  }
}
