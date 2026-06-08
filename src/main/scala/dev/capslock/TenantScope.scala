package dev.capslock

import dev.capslock.model.*

/** あるテナントのデータにアクセスする権限を表す capability。
  *
  * このインスタンスを保持している間だけ、当該テナント（`tenantId`）のデータに
  * アクセスできる。コンストラクタは `private` で、コンパニオンオブジェクトの
  * [[TenantScope.withTenant]] からのみ生成できる。ホスト名のテナント解決を経ずに
  * 勝手なスコープを作ることはできない（任意の `TenantId` でのスコープ偽装を防ぐ）。
  *
  * 横断的な capability のため、`dev.capslock` 直下に置く。配下のすべての層（model、
  * repository、repository.db）から参照でき、`Database` も `using` でこれを受け取って
  * ドメインモデル（scope に束縛された [[Email]] を含む [[User]]）を構築する。
  *
  * `caps.SharedCapability` を継承しているため capture checking の追跡対象となり、
  * [[TenantScope.withTenant]] のスコープの外へ持ち出そうとすると型エラーになる。
  */
class TenantScope private (val tenantId: TenantId)
    extends caps.SharedCapability

object TenantScope {

  /** ホスト名から解決したテナントのスコープを開き、その中で `body` を実行する。
    *
    * `body` は context function（`TenantScope^ ?=> T`）で、当該テナントの
    * [[TenantScope]] が given として渡される。Repository のメソッドはこの given を
    * `using` で受け取り、`scope.tenantId` に限定したアクセスのみを行う。このスコープは
    * `body` の実行中だけ有効であり、戻り値などを通じて外へ持ち出すと capture checking
    * が型エラーにする。
    *
    * @param host
    *   リクエストのホスト部分（例: `tenant1.app.example.com`）
    * @param body
    *   解決したテナントのスコープ内で実行する処理
    * @return
    *   ホストからテナントを解決できた場合は `body` の結果、できなければ [[scala.None]]
    */
  def withTenant[T](host: String)(body: TenantScope^ ?=> T): Option[T] =
    TenantResolver.resolve(host).map { tid =>
      given TenantScope = new TenantScope(tid)
      body
    }
}
