package dev.capslock.repository

import dev.capslock.TenantScope
import dev.capslock.model.*
import dev.capslock.repository.db.Database

/** ユーザーの永続化層。
  *
  * [[Database]] を呼び出すが、すべてのメソッドは given の [[TenantScope]] を要求し、
  * `scope.tenantId` に限定したアクセスのみを行う。テナントを引数で受け取らないため、
  * 他テナントのデータに到達・書き込みする経路が存在しない（scope による強制アクセス
  * 制御）。読み取り結果の `User` は `scope` を capture する（`User^{scope}`）ため、
  * スコープの外へ持ち出せない。
  */
final class UserRepository(db: Database) {

  /** 現在のスコープのテナントに属する全ユーザー。 */
  def all(using scope: TenantScope^): Seq[User^{scope}] =
    db.usersOf(scope.tenantId)

  /** 名前でユーザーを検索する（現在のスコープのテナント内のみ）。 */
  def findByName(name: String)(using scope: TenantScope^): Option[User^{scope}] =
    db.usersOf(scope.tenantId).find(_.name == name)

  /** 現在のスコープのテナントにユーザーを追加する。
    *
    * `tenantId` は引数で受け取らず `scope.tenantId` を強制的に付与するため、他テナントの
    * ユーザーとして書き込むことはできない。
    */
  def insert(name: String, email: String)(using scope: TenantScope^): Unit =
    db.insertUser((tenantId = scope.tenantId, name = name, email = email))
}

/** テナントの永続化層。
  *
  * [[UserRepository]] と同様に given の [[TenantScope]] を要求し、現在のスコープの
  * テナントのみを扱う。
  */
final class TenantRepository(db: Database) {

  /** 現在のスコープのテナント自身。 */
  def current(using scope: TenantScope^): Option[Tenant] =
    db.findTenant(scope.tenantId)
}
