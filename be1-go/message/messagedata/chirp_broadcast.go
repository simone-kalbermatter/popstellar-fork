package messagedata

import (
	"encoding/base64"

	"golang.org/x/xerrors"
)

// ChirpBroadcast defines a message data
type ChirpBroadcast struct {
	Object  string `json:"object"`
	Action  string `json:"action"`
	ChirpID string `json:"chirp_id"`
	Channel string `json:"channel"`

	// Timestamp is a Unix timestamp
	Timestamp int64 `json:"timestamp"`
}

// Verify verifies that the ChirpBroadcast message is correct
func (message ChirpBroadcast) Verify() error {
	// verify that Timestamp is positive
	if message.Timestamp < 0 {
		return xerrors.Errorf("timestamp is %d, should be minimum 0", message.Timestamp)
	}

	// verify that the chirp id is base64URL encoded
	_, err := base64.URLEncoding.DecodeString(message.ChirpID)
	if err != nil {
		return xerrors.Errorf("chirp id is %s, should be base64URL encoded", message.ChirpID)
	}

	return nil
}
