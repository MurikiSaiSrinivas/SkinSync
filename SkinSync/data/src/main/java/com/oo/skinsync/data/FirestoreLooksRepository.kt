package com.oo.skinsync.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.oo.skinsync.domain.LooksRepository
import com.oo.skinsync.domain.SavedLook
import com.oo.skinsync.domain.Suggestion
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Saved looks in Firestore under /users/{uid}/looks, scoped to an anonymous
 * user (rules in backend/firestore.rules). All Firebase I/O is here; the
 * document mapping is the pure, tested [LookMapping].
 */
@Singleton
class FirestoreLooksRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : LooksRepository {

    private suspend fun uid(): String {
        auth.currentUser?.let { return it.uid }
        return auth.signInAnonymously().await().user!!.uid
    }

    private fun collection(uid: String) =
        firestore.collection("users").document(uid).collection("looks")

    override fun observeLooks(): Flow<List<SavedLook>> = callbackFlow {
        val uid = runCatching { uid() }.getOrNull()
        if (uid == null) { trySend(emptyList()); awaitClose { }; return@callbackFlow }
        val reg = collection(uid)
            .orderBy(LookMapping.CREATED_AT, Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val items = snap?.documents?.map { LookMapping.fromDoc(it.id, it.data ?: emptyMap()) }
                trySend(items ?: emptyList())
            }
        awaitClose { reg.remove() }
    }.catch { emit(emptyList()) }.flowOn(Dispatchers.IO)

    override suspend fun save(location: String, suggestion: Suggestion): Result<Unit> = runCatching {
        val uid = uid()
        collection(uid).add(LookMapping.toMap(location, suggestion, System.currentTimeMillis())).await()
        Unit
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        collection(uid()).document(id).delete().await()
        Unit
    }

    override suspend fun setFavorite(id: String, favorite: Boolean): Result<Unit> = runCatching {
        collection(uid()).document(id).update(LookMapping.FAVORITE, favorite).await()
        Unit
    }
}
