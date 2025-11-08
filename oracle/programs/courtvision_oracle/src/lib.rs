use anchor_lang::prelude::*;

declare_id!("YOUR_PROGRAM_ID_PLACEHOLDER");

#[program]
pub mod courtvision_oracle {
    use super::*;

    /// Initialize the oracle data account to store winner announcements
    pub fn initialize_oracle(ctx: Context<InitializeOracle>) -> Result<()> {
        let oracle = &mut ctx.accounts.oracle_data;
        oracle.authority = ctx.accounts.authority.key();
        oracle.last_winner_announced = 0;
        oracle.total_announcements = 0;
        oracle.bump = ctx.bumps.oracle_data;

        msg!("Oracle initialized by {}", oracle.authority);
        Ok(())
    }

    /// Announce a league winner on-chain
    pub fn announce_winner(
        ctx: Context<AnnounceWinner>,
        league_id: u64,
        winner_address: Pubkey,
        final_score: u32,
    ) -> Result<()> {
        let oracle = &mut ctx.accounts.oracle_data;

        // Verify caller is authorized
        require_eq!(
            ctx.accounts.authority.key(),
            oracle.authority,
            OracleError::Unauthorized
        );

        // Update oracle state
        oracle.last_winner_announced = league_id;
        oracle.total_announcements = oracle.total_announcements.checked_add(1).unwrap();

        // Emit event
        emit!(WinnerAnnounced {
            league_id,
            winner: winner_address,
            final_score,
            timestamp: Clock::get()?.unix_timestamp,
        });

        msg!(
            "Winner announced for league {}: {} with score {}",
            league_id,
            winner_address,
            final_score
        );

        Ok(())
    }

    /// Retrieve oracle state (read-only)
    pub fn get_oracle_state(ctx: Context<GetOracleState>) -> Result<OracleStateData> {
        let oracle = &ctx.accounts.oracle_data;
        Ok(OracleStateData {
            authority: oracle.authority,
            last_winner_announced: oracle.last_winner_announced,
            total_announcements: oracle.total_announcements,
        })
    }
}

#[derive(Accounts)]
pub struct InitializeOracle<'info> {
    #[account(
        init,
        payer = authority,
        space = 8 + OracleData::INIT_SPACE,
        seeds = [b"oracle"],
        bump
    )]
    pub oracle_data: Account<'info, OracleData>,

    #[account(mut)]
    pub authority: Signer<'info>,

    pub system_program: Program<'info, System>,
}

#[derive(Accounts)]
pub struct AnnounceWinner<'info> {
    #[account(
        mut,
        seeds = [b"oracle"],
        bump = oracle_data.bump
    )]
    pub oracle_data: Account<'info, OracleData>,

    pub authority: Signer<'info>,
}

#[derive(Accounts)]
pub struct GetOracleState<'info> {
    pub oracle_data: Account<'info, OracleData>,
}

#[account]
#[derive(InitSpace)]
pub struct OracleData {
    pub authority: Pubkey,                 // 32 bytes
    pub last_winner_announced: u64,        // 8 bytes
    pub total_announcements: u64,          // 8 bytes
    pub bump: u8,                          // 1 byte
}

#[derive(AnchorSerialize, AnchorDeserialize, Clone)]
pub struct OracleStateData {
    pub authority: Pubkey,
    pub last_winner_announced: u64,
    pub total_announcements: u64,
}

#[event]
pub struct WinnerAnnounced {
    pub league_id: u64,
    pub winner: Pubkey,
    pub final_score: u32,
    pub timestamp: i64,
}

#[error_code]
pub enum OracleError {
    #[msg("You are not authorized to announce winners")]
    Unauthorized,

    #[msg("Invalid league ID")]
    InvalidLeagueId,

    #[msg("Invalid winner address")]
    InvalidWinnerAddress,
}
