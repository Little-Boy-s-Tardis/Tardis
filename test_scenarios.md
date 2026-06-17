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

--- ## Testing locally (localhost)
If you are running the Spring Boot backend service locally, replace `https://tardis-production.up.railway.app` with `http://localhost:8080` in the cURL or PowerShell commands.
