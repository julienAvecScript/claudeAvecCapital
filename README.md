# claudeAvecCapital
Buys low, sells high and holds your shit together

# CLAUDE WILL NEVER ASK FOR YOUR SECRET OR PASSPHRASE
1. Create an API key on pro.coinbase.com with View/Trade permissions and note down key, passphrase and secret
2. On Claude, go Create New Profile
3. On your OS explorer, go to the Profile directory and write the following JSON format in profile.claude (just open it with a dumb text editor):
{
  "key":"...",
  "passphrase":"...",
  "secret":"..."
}
4. Restart Claude
5. Load Profile > Use Profile Key to go in LIVE mode

# CURRENT BOUNTY
I will pay 20 USD in Bitcoin Cash to the first contributor that makes the Order POST signatures work.

# REFACTORING
Will happen after the POST signatures work. coinbase-java library references need to be cleaned up and should be ignored (CoinbaseClient class = cURL).
