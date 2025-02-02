package com.github.dedis.popstellar.repository.database.witnessing;

import androidx.room.*;

import com.github.dedis.popstellar.model.objects.WitnessMessage;
import com.github.dedis.popstellar.model.objects.security.MessageID;

import java.util.List;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface WitnessingDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  Completable insert(WitnessingEntity witnessingEntity);

  @Query("SELECT message FROM witness_messages WHERE lao_id = :laoId")
  Single<List<WitnessMessage>> getWitnessMessagesByLao(String laoId);

  @Query("DELETE FROM witness_messages WHERE lao_id = :laoId AND id IN (:filteredIds)")
  Completable deleteMessagesByIds(String laoId, Set<MessageID> filteredIds);
}
