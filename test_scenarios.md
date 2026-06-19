# Tardis Webhook Test Scenarios

Use this document to quickly copy test commands for sending webhook events to your backend service. You can run these commands from any folder on your machine.

--- ## Scenario 1: Technical Announcement (Importance: Medium)
* **Sender**: `Judge David (Tech Lead)`
* **Content**: Request to configure API prefix `/api/v1` and enable CORS.

### PowerShell
```powershell
$body = @{
  object = "whatsapp_business_account"
  entry = @(@{
    id = "test-msg-id-1"
    changes = @(@{
      value = @{
        messaging_product = "whatsapp"
        contacts = @(@{ profile = @{ name = "Judge David (Tech Lead)" }; wa_id = "84911112222" })
        messages = @(@{
          from = "84911112222"
          id = "wamid.testmsg1"
          timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString()
          text = @{ body = "Attention teams: The automated grading system is starting its scan. Ensure all Spring Boot controllers use the /api/v1 prefix and have CORS properly configured." }
          type = "text"
        })
      }
      field = "messages"
    })
  })
} | ConvertTo-Json -Depth 10
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
Invoke-RestMethod -Uri "https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp" -Method Post -ContentType "application/json" -Body $bodyBytes
```

### CMD
```cmd
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp -H "Content-Type: application/json" -d "{\"object\":\"whatsapp_business_account\",\"entry\":[{\"id\":\"test-msg-id-1\",\"changes\":[{\"value\":{\"messaging_product\":\"whatsapp\",\"contacts\":[{\"profile\":{\"name\":\"Judge David (Tech Lead)\"},\"wa_id\":\"84911112222\"}],\"messages\":[{\"from\":\"84911112222\",\"id\":\"wamid.testmsg1\",\"timestamp\":\"1697041663\",\"text\":{\"body\":\"Attention teams: The automated grading system is starting its scan. Ensure all Spring Boot controllers use the /api/v1 prefix and have CORS properly configured.\"},\"type\":\"text\"}]},\"field\":\"messages\"}]}]}"
```

--- ## Scenario 2: Emergency Deadline Extension (Importance: High)
* **Sender**: `Judge Minh (Chief)`
* **Content**: Extending submission deadline by 2 hours due to network issues.

### PowerShell
```powershell
$body = @{
  object = "whatsapp_business_account"
  entry = @(@{
    id = "test-msg-id-2"
    changes = @(@{
      value = @{
        messaging_product = "whatsapp"
        contacts = @(@{ profile = @{ name = "Judge Minh (Chief)" }; wa_id = "84933334444" })
        messages = @(@{
          from = "84933334444"
          id = "wamid.testmsg2"
          timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString()
          text = @{ body = "Emergency Announcement: Due to network connection congestion, the judges decided to extend the submission deadline by 2 hours. The new deadline is 24:00 tonight." }
          type = "text"
        })
      }
      field = "messages"
    })
  })
} | ConvertTo-Json -Depth 10
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
Invoke-RestMethod -Uri "https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp" -Method Post -ContentType "application/json" -Body $bodyBytes
```

### CMD
```cmd
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp -H "Content-Type: application/json" -d "{\"object\":\"whatsapp_business_account\",\"entry\":[{\"id\":\"test-msg-id-2\",\"changes\":[{\"value\":{\"messaging_product\":\"whatsapp\",\"contacts\":[{\"profile\":{\"name\":\"Judge Minh (Chief)\"},\"wa_id\":\"84933334444\"}],\"messages\":[{\"from\":\"84933334444\",\"id\":\"wamid.testmsg2\",\"timestamp\":\"1697041663\",\"text\":{\"body\":\"Emergency Announcement: Due to network connection congestion, the judges decided to extend the submission deadline by 2 hours. The new deadline is 24:00 tonight.\"},\"type\":\"text\"}]},\"field\":\"messages\"}]}]}"
```

--- ## Scenario 3: Q&A Announcement (Importance: Low)
* **Sender**: `Judge Jessica (Design Coach)`
* **Content**: Hosting a Q&A discussion on Discord voice channel at 16:00.

### PowerShell
```powershell
$body = @{
  object = "whatsapp_business_account"
  entry = @(@{
    id = "test-msg-id-3"
    changes = @(@{
      value = @{
        messaging_product = "whatsapp"
        contacts = @(@{ profile = @{ name = "Judge Jessica (Design Coach)" }; wa_id = "84955556666" })
        messages = @(@{
          from = "84955556666"
          id = "wamid.testmsg3"
          timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString()
          text = @{ body = "We will host a brief Q&A session at 16:00 today on the Discord voice channel to guide teams on optimizing user interfaces and UI/UX design layouts." }
          type = "text"
        })
      }
      field = "messages"
    })
  })
} | ConvertTo-Json -Depth 10
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
Invoke-RestMethod -Uri "https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp" -Method Post -ContentType "application/json" -Body $bodyBytes
```

### CMD
```cmd
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp -H "Content-Type: application/json" -d "{\"object\":\"whatsapp_business_account\",\"entry\":[{\"id\":\"test-msg-id-3\",\"changes\":[{\"value\":{\"messaging_product\":\"whatsapp\",\"contacts\":[{\"profile\":{\"name\":\"Judge Jessica (Design Coach)\"},\"wa_id\":\"84955556666\"}],\"messages\":[{\"from\":\"84955556666\",\"id\":\"wamid.testmsg3\",\"timestamp\":\"1697041663\",\"text\":{\"body\":\"We will host a brief Q&A session at 16:00 today on the Discord voice channel to guide teams on optimizing user interfaces and UI/UX design layouts.\"},\"type\":\"text\"}]},\"field\":\"messages\"}]}]}"
```

--- ## Scenario 4: Emergency System Alert (Importance: High)
* **Sender**: `Judge David (Tech Lead)`
* **Content**: Scheduled maintenance downtime alert.

### PowerShell
```powershell
$body = @{
  object = "whatsapp_business_account"
  entry = @(@{
    id = "test-msg-id-4"
    changes = @(@{
      value = @{
        messaging_product = "whatsapp"
        contacts = @(@{ profile = @{ name = "Judge David (Tech Lead)" }; wa_id = "84911112222" })
        messages = @(@{
          from = "84911112222"
          id = "wamid.testmsg4"
          timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString()
          text = @{ body = "Emergency Alert: The submission portal will be temporarily closed for unexpected maintenance in the next 15 minutes. Please halt all pushing until further notice." }
          type = "text"
        })
      }
      field = "messages"
    })
  })
} | ConvertTo-Json -Depth 10
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
Invoke-RestMethod -Uri "https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp" -Method Post -ContentType "application/json" -Body $bodyBytes
```

### CMD
```cmd
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp -H "Content-Type: application/json" -d "{\"object\":\"whatsapp_business_account\",\"entry\":[{\"id\":\"test-msg-id-4\",\"changes\":[{\"value\":{\"messaging_product\":\"whatsapp\",\"contacts\":[{\"profile\":{\"name\":\"Judge David (Tech Lead)\"},\"wa_id\":\"84911112222\"}],\"messages\":[{\"from\":\"84911112222\",\"id\":\"wamid.testmsg4\",\"timestamp\":\"1697041663\",\"text\":{\"body\":\"Emergency Alert: The submission portal will be temporarily closed for unexpected maintenance in the next 15 minutes. Please halt all pushing until further notice.\"},\"type\":\"text\"}]},\"field\":\"messages\"}]}]}"
```

--- ## Scenario 5: Urgent Rule Modification (Importance: High)
* **Sender**: `Judge Jessica (Design Coach)`
* **Content**: Urgent design evaluation guidelines modification.

### PowerShell
```powershell
$body = @{
  object = "whatsapp_business_account"
  entry = @(@{
    id = "test-msg-id-5"
    changes = @(@{
      value = @{
        messaging_product = "whatsapp"
        contacts = @(@{ profile = @{ name = "Judge Jessica (Design Coach)" }; wa_id = "84955556666" })
        messages = @(@{
          from = "84955556666"
          id = "wamid.testmsg5"
          timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString()
          text = @{ body = "Urgent update for all teams: We have updated the UI/UX design checklist. Please review it immediately to ensure compliance with contrast standards." }
          type = "text"
        })
      }
      field = "messages"
    })
  })
} | ConvertTo-Json -Depth 10
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
Invoke-RestMethod -Uri "https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp" -Method Post -ContentType "application/json" -Body $bodyBytes
```

### CMD
```cmd
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp -H "Content-Type: application/json" -d "{\"object\":\"whatsapp_business_account\",\"entry\":[{\"id\":\"test-msg-id-5\",\"changes\":[{\"value\":{\"messaging_product\":\"whatsapp\",\"contacts\":[{\"profile\":{\"name\":\"Judge Jessica (Design Coach)\"},\"wa_id\":\"84955556666\"}],\"messages\":[{\"from\":\"84955556666\",\"id\":\"wamid.testmsg5\",\"timestamp\":\"1697041663\",\"text\":{\"body\":\"Urgent update for all teams: We have updated the UI/UX design checklist. Please review it immediately to ensure compliance with contrast standards.\"},\"type\":\"text\"}]},\"field\":\"messages\"}]}]}"
```

--- ## Scenario 7: Discord Server Reboot (Importance: Medium)
* **Sender**: `Judge Techbot`
* **Content**: Notice of server restart.

### PowerShell
```powershell
$body = @{
  sender = "Judge Techbot"
  content = "The submission evaluation server will undergo a restart to apply the latest API patch. Expected downtime is 5 minutes."
  conversationId = "global-discord-channel"
} | ConvertTo-Json
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
Invoke-RestMethod -Uri "https://tardis-production.up.railway.app/api/v1/webhooks/discord" -Method Post -ContentType "application/json" -Headers @{ "X-Webhook-Token" = "tardis-secret-verify-token" } -Body $bodyBytes
```

### CMD
```cmd
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/discord -H "Content-Type: application/json" -H "X-Webhook-Token: tardis-secret-verify-token" -d "{\"sender\":\"Judge Techbot\",\"content\":\"The submission evaluation server will undergo a restart to apply the latest API patch. Expected downtime is 5 minutes.\",\"conversationId\":\"global-discord-channel\"}"
```

--- ## Scenario 8: Discord Critical Security Bug (Importance: High)
* **Sender**: `Judge Minh (Chief)`
* **Content**: Emergency alert regarding an exploit.

### PowerShell
```powershell
$body = @{
  sender = "Judge Minh (Chief)"
  content = "Emergency Notice: A critical vulnerability was found in the starter template. Teams must update their Docker compose configurations immediately or face a penalty!"
  conversationId = "global-discord-channel"
} | ConvertTo-Json
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
Invoke-RestMethod -Uri "https://tardis-production.up.railway.app/api/v1/webhooks/discord" -Method Post -ContentType "application/json" -Headers @{ "X-Webhook-Token" = "tardis-secret-verify-token" } -Body $bodyBytes
```

### CMD
```cmd
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/discord -H "Content-Type: application/json" -H "X-Webhook-Token: tardis-secret-verify-token" -d "{\"sender\":\"Judge Minh (Chief)\",\"content\":\"Emergency Notice: A critical vulnerability was found in the starter template. Teams must update their Docker compose configurations immediately or face a penalty!\",\"conversationId\":\"global-discord-channel\"}"
```

--- ## Scenario 9: Discord Optional AMA Session (Importance: Low)
* **Sender**: `Judge Jessica`
* **Content**: Invitation to a casual AMA session.

### PowerShell
```powershell
$body = @{
  sender = "Judge Jessica"
  content = "Just a quick reminder: We are hosting a casual Ask-Me-Anything session tonight. It is completely optional, just FYI for anyone who wants to hang out and talk about design."
  conversationId = "global-discord-channel"
} | ConvertTo-Json
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
Invoke-RestMethod -Uri "https://tardis-production.up.railway.app/api/v1/webhooks/discord" -Method Post -ContentType "application/json" -Headers @{ "X-Webhook-Token" = "tardis-secret-verify-token" } -Body $bodyBytes
```

### CMD
```cmd
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/discord -H "Content-Type: application/json" -H "X-Webhook-Token: tardis-secret-verify-token" -d "{\"sender\":\"Judge Jessica\",\"content\":\"Just a quick reminder: We are hosting a casual Ask-Me-Anything session tonight. It is completely optional, just FYI for anyone who wants to hang out and talk about design.\",\"conversationId\":\"global-discord-channel\"}"
```

--- ## Scenario 10: Discord Registration Issue (Importance: High)
* **Sender**: `Judge Sarah`
* **Content**: Important alert about team registration.

### PowerShell
```powershell
$body = @{
  sender = "Judge Sarah"
  content = "🚨 URGENT: The team registration portal is currently experiencing a critical database failure. All teams who registered in the last 30 minutes MUST re-submit their forms immediately. Deadline for re-submission is strictly 18:00 today."
  conversationId = "global-discord-channel"
} | ConvertTo-Json
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
Invoke-RestMethod -Uri "https://tardis-production.up.railway.app/api/v1/webhooks/discord" -Method Post -ContentType "application/json" -Headers @{ "X-Webhook-Token" = "tardis-secret-verify-token" } -Body $bodyBytes
```

### CMD
```cmd
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/discord -H "Content-Type: application/json" -H "X-Webhook-Token: tardis-secret-verify-token" -d "{\"sender\":\"Judge Sarah\",\"content\":\"🚨 URGENT: The team registration portal is currently experiencing a critical database failure. All teams who registered in the last 30 minutes MUST re-submit their forms immediately. Deadline for re-submission is strictly 18:00 today.\",\"conversationId\":\"global-discord-channel\"}"
```

--- ## Scenario 11: WhatsApp Judging Criteria Update (Importance: Medium)
* **Sender**: `Judge Mark (Technical)`
* **Content**: Clarification on grading rules.

### PowerShell
```powershell
$body = @{
  object = "whatsapp_business_account"
  entry = @(@{
    id = "test-msg-id-11"
    changes = @(@{
      value = @{
        messaging_product = "whatsapp"
        contacts = @(@{ profile = @{ name = "Judge Mark (Technical)" }; wa_id = "84977778888" })
        messages = @(@{
          from = "84977778888"
          id = "wamid.testmsg11"
          timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString()
          text = @{ body = "Please note that the judging criteria have been slightly updated. Code quality now accounts for 30% of the total score instead of 20%. Ensure your repositories are well-documented before the final review." }
          type = "text"
        })
      }
      field = "messages"
    })
  })
} | ConvertTo-Json -Depth 10
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
Invoke-RestMethod -Uri "https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp" -Method Post -ContentType "application/json" -Body $bodyBytes
```

### CMD
```cmd
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp -H "Content-Type: application/json" -d "{\"object\":\"whatsapp_business_account\",\"entry\":[{\"id\":\"test-msg-id-11\",\"changes\":[{\"value\":{\"messaging_product\":\"whatsapp\",\"contacts\":[{\"profile\":{\"name\":\"Judge Mark (Technical)\"},\"wa_id\":\"84977778888\"}],\"messages\":[{\"from\":\"84977778888\",\"id\":\"wamid.testmsg11\",\"timestamp\":\"1697041663\",\"text\":{\"body\":\"Please note that the judging criteria have been slightly updated. Code quality now accounts for 30% of the total score instead of 20%. Ensure your repositories are well-documented before the final review.\"},\"type\":\"text\"}]},\"field\":\"messages\"}]}]}"
```

--- ## Scenario 12: Discord Free Food (Importance: Low)
* **Sender**: `Event Staff`
* **Content**: Free pizza notification.

### PowerShell
```powershell
$body = @{
  sender = "Event Staff"
  content = "Hey everyone, we just ordered 50 boxes of pizza. They have arrived in the main lobby! Come and grab a slice to recharge for the night. First come, first served!"
  conversationId = "global-discord-channel"
} | ConvertTo-Json
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
Invoke-RestMethod -Uri "https://tardis-production.up.railway.app/api/v1/webhooks/discord" -Method Post -ContentType "application/json" -Headers @{ "X-Webhook-Token" = "tardis-secret-verify-token" } -Body $bodyBytes
```

### CMD
```cmd
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/discord -H "Content-Type: application/json" -H "X-Webhook-Token: tardis-secret-verify-token" -d "{\"sender\":\"Event Staff\",\"content\":\"Hey everyone, we just ordered 50 boxes of pizza. They have arrived in the main lobby! Come and grab a slice to recharge for the night. First come, first served!\",\"conversationId\":\"global-discord-channel\"}"
```

--- ## Testing locally (localhost)
If you are running the Spring Boot backend service locally, replace `https://tardis-production.up.railway.app` with `http://localhost:8080` in the cURL or PowerShell commands.
