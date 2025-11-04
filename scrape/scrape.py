import re
import pandas as pd
import os

# ==========================================
# NBA Player Data Scraper
# Converts unformatted text file to clean CSV
# ==========================================

class NBAPlayerScraper:
    """Scrapes NBA player data from text file and exports to CSV."""

    def __init__(self, input_file="players.txt", output_dir=None):
        """
        Initialize the scraper.

        Args:
            input_file: Path to the input text file containing player data
            output_dir: Directory to save the output CSV (defaults to script directory)
        """
        self.input_file = input_file
        self.output_dir = output_dir or os.path.dirname(os.path.abspath(__file__))
        self.output_file = os.path.join(self.output_dir, "nba_players.csv")
        self.players_list = []
        self.debug = True

    def load_input_file(self):
        """Load and read the input text file."""
        try:
            with open(self.input_file, "r", encoding="utf-8") as file:
                text = file.read()

            if self.debug:
                print(f"[OK] Successfully loaded {self.input_file}")
                print(f"     File size: {len(text)} characters")

            return text
        except FileNotFoundError:
            print(f"[ERROR] File '{self.input_file}' not found.")
            raise
        except Exception as e:
            print(f"[ERROR] Error reading file: {str(e)}")
            raise

    def clean_text(self, text):
        """
        Clean the input text by removing unwanted patterns.
        Skip the header row and normalize the format.

        Args:
            text: Raw text to clean

        Returns:
            List of cleaned player line data
        """
        lines = text.strip().split('\n')

        # Skip the header row (first line)
        if lines and 'Player' in lines[0] and 'Team' in lines[0]:
            lines = lines[1:]

        if self.debug:
            print(f"[OK] Text cleaned successfully")
            print(f"     Total lines to process: {len(lines)}")

        return lines

    def extract_player_data(self, lines_list):
        """
        Extract player information from cleaned lines.

        The file format is:
        PlayerName Headshot
        FirstName

        LastName

        Team	Number	Position	Height	Weight	College	Country

        Args:
            lines_list: List of lines from the cleaned text

        Returns:
            List of dictionaries containing player data
        """
        players = []
        i = 0

        while i < len(lines_list):
            line = lines_list[i].strip()

            # Skip empty lines
            if not line:
                i += 1
                continue

            # Check if this line contains a player name with "Headshot"
            if 'Headshot' in line:
                # Extract the player full name (everything before "Headshot")
                player_full_name = line.replace('Headshot', '').strip()

                # Look ahead to find the tab-separated data row
                j = i + 1
                tab_data_row = None

                # Keep looking forward until we find a line with tabs (the data row)
                while j < len(lines_list):
                    current_line = lines_list[j].strip()

                    if '\t' in current_line and current_line:
                        tab_data_row = current_line
                        break
                    j += 1

                # If we found the tab-separated data, parse it
                if tab_data_row:
                    parts = tab_data_row.split('\t')

                    if len(parts) >= 7:  # Ensure we have all required fields
                        try:
                            player_record = {
                                "player_name": player_full_name,
                                "team_code": parts[0].strip(),
                                "jersey_number": int(parts[1].strip()) if parts[1].strip().isdigit() else None,
                                "position": parts[2].strip(),
                                "height_feet_inches": parts[3].strip(),
                                "weight_pounds": parts[4].strip(),
                                "college_name": parts[5].strip(),
                                "country_name": parts[6].strip()
                            }
                            players.append(player_record)
                        except (ValueError, IndexError) as e:
                            if self.debug:
                                print(f"[WARN] Could not parse line at index {j}: {tab_data_row}")

                i = j + 1
            else:
                i += 1

        if self.debug:
            print(f"[OK] Pattern matching completed")
            print(f"     Found {len(players)} player records")

        return players

    def format_player_records(self, player_data):
        """
        Validate and store player records that are already formatted.

        Args:
            player_data: List of dictionaries with player data (already formatted)

        Returns:
            List of dictionaries with validated player data
        """
        self.players_list = player_data

        if self.debug:
            print(f"[OK] Validated {len(self.players_list)} player records")

        return self.players_list

    def create_dataframe(self):
        """
        Create a pandas DataFrame from the players list.

        Returns:
            DataFrame with player data
        """
        if not self.players_list:
            print("[WARNING] No players to create DataFrame")
            return pd.DataFrame()

        dataframe = pd.DataFrame(self.players_list)

        if self.debug:
            print(f"[OK] DataFrame created with shape: {dataframe.shape}")

        return dataframe

    def save_to_csv(self, dataframe):
        """
        Save the DataFrame to a CSV file in the output directory.

        Args:
            dataframe: DataFrame to save
        """
        try:
            # Ensure output directory exists
            os.makedirs(self.output_dir, exist_ok=True)

            # Save with index=False to exclude row numbers
            dataframe.to_csv(self.output_file, index=False)

            if self.debug:
                print(f"[OK] CSV saved successfully: {self.output_file}")
                print(f"     Total players exported: {len(dataframe)}")

            return self.output_file
        except Exception as e:
            print(f"[ERROR] Error saving CSV: {str(e)}")
            raise

    def display_sample_data(self, dataframe, num_rows=5):
        """
        Display sample data from the DataFrame for verification.

        Args:
            dataframe: DataFrame to sample from
            num_rows: Number of rows to display
        """
        if len(dataframe) == 0:
            print("No data to display")
            return

        print(f"\n[SAMPLE DATA] First {min(num_rows, len(dataframe))} rows:")
        print("=" * 120)
        print(dataframe.head(num_rows).to_string())
        print("=" * 120)

    def run(self):
        """Execute the complete scraping and export process."""
        try:
            print("\n[START] NBA Player Scraper Starting...")
            print("=" * 120)

            # Step 1: Load input file
            raw_text = self.load_input_file()

            # Step 2: Clean text
            cleaned_text = self.clean_text(raw_text)

            # Step 3: Extract player data using regex
            player_matches = self.extract_player_data(cleaned_text)

            if not player_matches:
                print("[WARNING] No players found. Check the input file format.")
                return False

            # Step 4: Format into structured records
            self.format_player_records(player_matches)

            # Step 5: Create DataFrame
            dataframe = self.create_dataframe()

            # Step 6: Save to CSV
            self.save_to_csv(dataframe)

            # Step 7: Display sample
            self.display_sample_data(dataframe)

            print("\n[SUCCESS] Process completed successfully!")
            print("=" * 120 + "\n")

            return True

        except Exception as e:
            print(f"\n[FATAL] Fatal error: {str(e)}")
            print("=" * 120 + "\n")
            return False


def main():
    """Main entry point for the NBA player scraper."""
    # Create scraper instance (uses current directory as output)
    scraper = NBAPlayerScraper(
        input_file="players.txt",
        output_dir=os.path.dirname(os.path.abspath(__file__))
    )

    # Run the scraper
    success = scraper.run()

    # Exit with appropriate code
    exit(0 if success else 1)


if __name__ == "__main__":
    main()
