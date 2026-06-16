# Tardis Webhook Test Scenarios

Sử dụng tài liệu này để copy nhanh các lệnh thử nghiệm gửi sự kiện webhook đến hệ thống backend của bạn. Bạn có thể chạy chúng ở bất kỳ thư mục nào trên máy tính.

---

## Kịch bản 1: Thông báo kỹ thuật (Độ ưu tiên: Trung bình)
* **Người gửi**: `Judge David (Tech Lead)`
* **Nội dung**: Yêu cầu cấu hình API prefix `/api/v1` và bật CORS.

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
          text = @{ body = "Các đội lưu ý: Hệ thống chấm điểm tự động sẽ bắt đầu quét. Yêu cầu các Spring Boot controller phải sử dụng tiền tố /api/v1 và cấu hình CORS đầy đủ." }
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
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp -H "Content-Type: application/json" -d "{\"object\":\"whatsapp_business_account\",\"entry\":[{\"id\":\"test-msg-id-1\",\"changes\":[{\"value\":{\"messaging_product\":\"whatsapp\",\"contacts\":[{\"profile\":{\"name\":\"Judge David (Tech Lead)\"},\"wa_id\":\"84911112222\"}],\"messages\":[{\"from\":\"84911112222\",\"id\":\"wamid.testmsg1\",\"timestamp\":\"1697041663\",\"text\":{\"body\":\"Cac doi luu y: He thong cham diem tu dong se bat dau quet. Yeu cau cac Spring Boot controller phai su dung tien to /api/v1 va cau hinh CORS day du.\"},\"type\":\"text\"}]},\"field\":\"messages\"}]}]}"
```

---

## Kịch bản 2: Thông báo khẩn cấp gia hạn thời gian (Độ ưu tiên: Cao)
* **Người gửi**: `Judge Minh (Trưởng Ban)`
* **Nội dung**: Gia hạn nộp bài thi thêm 2 tiếng do sự cố mạng.

### PowerShell
```powershell
$body = @{
  object = "whatsapp_business_account"
  entry = @(@{
    id = "test-msg-id-2"
    changes = @(@{
      value = @{
        messaging_product = "whatsapp"
        contacts = @(@{ profile = @{ name = "Judge Minh (Trưởng Ban)" }; wa_id = "84933334444" })
        messages = @(@{
          from = "84933334444"
          id = "wamid.testmsg2"
          timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString()
          text = @{ body = "Thông báo khẩn cấp: Do hệ thống mạng bị nghẽn đường truyền, Ban tổ chức quyết định gia hạn thời gian nộp bài thi thêm 2 tiếng. Hạn chót mới là 24:00 đêm nay." }
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
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp -H "Content-Type: application/json" -d "{\"object\":\"whatsapp_business_account\",\"entry\":[{\"id\":\"test-msg-id-2\",\"changes\":[{\"value\":{\"messaging_product\":\"whatsapp\",\"contacts\":[{\"profile\":{\"name\":\"Judge Minh (Truong Ban)\"},\"wa_id\":\"84933334444\"}],\"messages\":[{\"from\":\"84933334444\",\"id\":\"wamid.testmsg2\",\"timestamp\":\"1697041663\",\"text\":{\"body\":\"Thong bao khan cap: Do he thong mang bi nghen duong truyen, Ban to chuc quyet dinh gia han thoi gian nop bai thi them 2 tieng. Han chot moi la 24:00 dem nay.\"},\"type\":\"text\"}]},\"field\":\"messages\"}]}]}"
```

---

## Kịch bản 3: Thông báo Q&A UI/UX (Độ ưu tiên: Thấp)
* **Người gửi**: `Judge Jessica (Design Coach)`
* **Nội dung**: Tổ chức buổi thảo luận UI/UX trên Discord lúc 16:00.

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
          text = @{ body = "Chúng tôi sẽ mở một buổi thảo luận Q&A ngắn vào lúc 16:00 chiều nay trên kênh voice Discord để hướng dẫn các đội tối ưu hóa giao diện thiết kế và trải nghiệm người dùng UI/UX." }
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
curl -X POST https://tardis-production.up.railway.app/api/v1/webhooks/whatsapp -H "Content-Type: application/json" -d "{\"object\":\"whatsapp_business_account\",\"entry\":[{\"id\":\"test-msg-id-3\",\"changes\":[{\"value\":{\"messaging_product\":\"whatsapp\",\"contacts\":[{\"profile\":{\"name\":\"Judge Jessica (Design Coach)\"},\"wa_id\":\"84955556666\"}],\"messages\":[{\"from\":\"84955556666\",\"id\":\"wamid.testmsg3\",\"timestamp\":\"1697041663\",\"text\":{\"body\":\"Chung toi se mo mot buoi thao luan Q&A ngan vao luc 16:00 chieu nay tren kenh voice Discord de huong dan cac doi toi uu hoa giao dien thiet ke va trai nghiem nguoi dung UI/UX.\"},\"type\":\"text\"}]},\"field\":\"messages\"}]}]}"
```

---

## Cách chạy thử nghiệm trên máy Local (localhost)
Nếu chạy backend Spring Boot local, chỉ cần thay đổi địa chỉ domain từ `https://tardis-production.up.railway.app` thành `http://localhost:8080` ở dòng `Invoke-RestMethod` hoặc `curl`.
