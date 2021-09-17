package query

import message "student20_pop/message"

const (
	MethodPublish     = "publish"
	MethodSubscribe   = "subscribe"
	MethodUnsubscribe = "unsubscribe"
	MethodCatchUp     = "catchup"
	MethodBroadcast   = "broadcast"
)

// Base defines all the common attributes for a Query RPC message
type Base struct {
	message.JSONRPCBase

	Method string `json:"method"`
}
