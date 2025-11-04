package com.courtvision.repository;

import com.courtvision.entity.DraftPick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DraftPickRepository extends JpaRepository<DraftPick, Long> {

    /**
     * Find all picks for a draft, ordered by round and pick number
     */
    @Query("SELECT dp FROM DraftPick dp WHERE dp.draft.id = :draftId ORDER BY dp.roundNumber ASC, dp.pickNumber ASC")
    List<DraftPick> findByDraftIdOrdered(@Param("draftId") Long draftId);

    /**
     * Find all picks for a draft and user
     */
    List<DraftPick> findByDraftIdAndPickerId(Long draftId, Long pickerId);

    /**
     * Count picks in a draft
     */
    long countByDraftId(Long draftId);

    /**
     * Count picks made by a user in a draft
     */
    long countByDraftIdAndPickerId(Long draftId, Long pickerId);

    /**
     * Find picks for a round
     */
    List<DraftPick> findByDraftIdAndRoundNumber(Long draftId, Integer roundNumber);

    /**
     * Check if a player has been drafted
     */
    boolean existsByDraftIdAndPlayerName(Long draftId, String playerName);
}
