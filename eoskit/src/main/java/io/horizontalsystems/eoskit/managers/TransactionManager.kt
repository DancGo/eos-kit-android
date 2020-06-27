package io.horizontalsystems.eoskit.managers

import io.horizontalsystems.eoskit.core.ErrorUtils
import io.reactivex.Single
import one.block.eosiojava.error.session.TransactionSignAndBroadCastError
import one.block.eosiojava.interfaces.IABIProvider
import one.block.eosiojava.interfaces.IRPCProvider
import one.block.eosiojava.interfaces.ISerializationProvider
import one.block.eosiojava.interfaces.ISignatureProvider
import one.block.eosiojava.models.rpcProvider.Action
import one.block.eosiojava.models.rpcProvider.Authorization
import one.block.eosiojava.session.TransactionSession
import org.json.JSONObject

class TransactionManager(
    private val rpcProvider: IRPCProvider,
    private val signatureProvider: ISignatureProvider,
    private val serializationProvider: ISerializationProvider,
    private val abiProvider: IABIProvider
) {

    @Throws
    fun send(account: String, token: String, to: String, quantity: String, memo: String): Single<String> {
        return Single.create { it.onSuccess(process(account, token, to, quantity, memo)) }
    }
    @Throws
    fun send2(account: String, token: String, publicKey: String): Single<String> {
        return Single.create { it.onSuccess(process2(account, token, publicKey)) }
    }
    @Throws
    private fun process2(name: String, token: String, publicKey: String): String {
        val session = TransactionSession(serializationProvider, rpcProvider, abiProvider, signatureProvider)
        val processor = session.transactionProcessor
        var str:String = "vid" + name
        val reqJson = "{\"creator\":\"liyan1234511\",\"name\":\""+name+"\",\"owner\":{\"threshold\":1,\"keys\": [{ \"key\": \"BACC5yCG6MPUSf8p34bNQbU4yueW1hQpazHgyMFEWJtsDbzWNGXfzQ\",\"weight\":1}],\"accounts\":[],\"waits\":[]},\"active\":{\"threshold\":1,\"keys\":[{\"key\":\"BACC5yCG6MPUSf8p34bNQbU4yueW1hQpazHgyMFEWJtsDbzWNGXfzQ\",\"weight\":1}],\"accounts\":[],\"waits\":[]},\"vid\":\""+str+"\"}"
        val action = Action(token, "newaccount", listOf(Authorization("liyan1234511", "active")), reqJson.toString())

        try {
            //  Prepare actions with above actions. A actions can be executed with multiple actions.
            processor.prepare(listOf(action))
            //  Sign and broadcast the actions.
            val response = processor.signAndBroadcast()
            return response.transactionId
        } catch (e: TransactionSignAndBroadCastError) {
            val rpcResponseError = ErrorUtils.getBackendError(e)
            if (rpcResponseError != null) {
                throw ErrorUtils.getBackendErrorFromResponse(rpcResponseError)
            }
        }

        throw Exception()
    }
    @Throws
    private fun process(account: String, token: String, to: String, quantity: String, memo: String): String {

        val session = TransactionSession(serializationProvider, rpcProvider, abiProvider, signatureProvider)
        val processor = session.transactionProcessor

        //  Apply actions data to Action's data
        val reqJson = JSONObject().apply {
            put("from", account)
            put("to", to)
            put("quantity", quantity)
            put("memo", memo)
        }

        val action = Action(token, "transfer", listOf(Authorization(account, "active")), reqJson.toString())

        try {
            //  Prepare actions with above actions. A actions can be executed with multiple actions.
            processor.prepare(listOf(action))
            //  Sign and broadcast the actions.
            val response = processor.signAndBroadcast()
            return response.transactionId
        } catch (e: TransactionSignAndBroadCastError) {
            val rpcResponseError = ErrorUtils.getBackendError(e)
            if (rpcResponseError != null) {
                throw ErrorUtils.getBackendErrorFromResponse(rpcResponseError)
            }
        }

        throw Exception()
    }


}
