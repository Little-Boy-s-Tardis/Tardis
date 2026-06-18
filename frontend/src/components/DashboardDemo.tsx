import React, { useState, useMemo, useEffect } from 'react';
import { Send, Search, SlidersHorizontal, Sparkles, MessageSquare, AlertCircle, BarChart2, ShieldAlert, Globe } from 'lucide-react';
import './DashboardDemo.css';

interface Announcement {
  id: string;
  sender: string;
  platform: 'DISCORD' | 'WHATSAPP';
  timestamp: string;
  originalMessage: string;
  aiSummary: string[];
  tags: string[];
  importance: 'HIGH' | 'MEDIUM' | 'LOW';
}

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const WS_URL = import.meta.env.VITE_WS_URL || (API_BASE_URL.startsWith('https') 
  ? `wss://${API_BASE_URL.replace(/^https?:\/\//, '')}/ws`
  : `ws://${API_BASE_URL.replace(/^https?:\/\//, '')}/ws`);

const localTz = (() => {
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone;
  } catch {
    return 'UTC';
  }
})();

const TIMEZONES_LIST = [
  { value: 'Asia/Ho_Chi_Minh', label: 'Ho Chi Minh City (GMT+7)' },
  { value: 'UTC', label: 'UTC / GMT' },
  { value: 'Asia/Singapore', label: 'Singapore (GMT+8)' },
  { value: 'Asia/Tokyo', label: 'Tokyo (GMT+9)' },
  { value: 'Europe/London', label: 'London (GMT+0/+1)' },
  { value: 'America/New_York', label: 'New York (GMT-5/-4)' },
  { value: 'America/Los_Angeles', label: 'Los Angeles (GMT-8/-7)' }
];

const getUniqueTimezones = () => {
  const list = [...TIMEZONES_LIST];
  if (localTz && !list.some(tz => tz.value === localTz)) {
    list.push({ value: localTz, label: `Local (${localTz})` });
  }
  return list;
};

const UNIQUE_TIMEZONES = getUniqueTimezones();

const formatTimestamp = (timestampStr: string, timeZone: string) => {
  if (!timestampStr) return '';
  if (timestampStr === 'Just now') return 'Just now';
  
  try {
    const date = new Date(timestampStr);
    if (isNaN(date.getTime())) return timestampStr;
    
    return new Intl.DateTimeFormat('en-US', {
      timeZone: timeZone,
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    }).format(date);
  } catch (error) {
    console.error("Error formatting date:", error);
    return timestampStr;
  }
};

export function DashboardDemo() {
  const [announcements, setAnnouncements] = useState<Announcement[]>([]);
  const [search, setSearch] = useState('');
  const [platformFilter, setPlatformFilter] = useState<'ALL' | 'DISCORD' | 'WHATSAPP'>('ALL');
  const [importanceFilter, setImportanceFilter] = useState<'ALL' | 'HIGH' | 'MEDIUM' | 'LOW'>('ALL');
  const [selectedTimezone, setSelectedTimezone] = useState<string>('Asia/Ho_Chi_Minh');
  
  // Simulator form state
  const [simSender, setSimSender] = useState('');
  const [simPlatform, setSimPlatform] = useState<'DISCORD' | 'WHATSAPP'>('DISCORD');
  const [simImportance, setSimImportance] = useState<'HIGH' | 'MEDIUM' | 'LOW'>('MEDIUM');
  const [simMessage, setSimMessage] = useState('');
  
  // Simulation status pipeline
  const [simStep, setSimStep] = useState<number>(0); // 0: idle, 1: webhook, 2: rabbitmq, 3: ai summarizer, 4: broadcasting
  const [expandedCards, setExpandedCards] = useState<Record<string, boolean>>({});

  const toggleExpand = (id: string) => {
    setExpandedCards(prev => ({ ...prev, [id]: !prev[id] }));
  };

  useEffect(() => {
    // 1. Fetch historical announcements
    fetch(`${API_BASE_URL}/api/v1/announcements`)
      .then(res => res.json())
      .then(data => {
        if (Array.isArray(data) && data.length > 0) {
          setAnnouncements(data);
        }
      })
      .catch(err => console.warn("Backend offline. Operating in simulation mode with mock data.", err));

    // 2. STOMP over WebSocket connection
    let ws: WebSocket;
    let reconnectTimeout: ReturnType<typeof setTimeout> | undefined;

    const connectWebSocket = () => {
      console.log("Connecting to WebSocket stomp endpoint...");
      ws = new WebSocket(WS_URL);

      ws.onopen = () => {
        console.log("WebSocket open. Connecting STOMP...");
        ws.send("CONNECT\naccept-version:1.1,1.2\nheart-beat:10000,10000\n\n\u0000");
      };

      ws.onmessage = (event) => {
        const msg = event.data;
        if (msg.startsWith("CONNECTED")) {
          console.log("STOMP session connected. Subscribing to /topic/announcements...");
          ws.send("SUBSCRIBE\nid:sub-0\ndestination:/topic/announcements\n\n\u0000");
        } else if (msg.startsWith("MESSAGE")) {
          const bodyStartIndex = msg.indexOf("\n\n") + 2;
          const bodyEndIndex = msg.lastIndexOf("\u0000");
          if (bodyStartIndex > 1 && bodyEndIndex > -1) {
            const jsonBody = msg.substring(bodyStartIndex, bodyEndIndex).trim();
            try {
              const newAnnouncement = JSON.parse(jsonBody);
              console.log("Live message received:", newAnnouncement);
              setAnnouncements(prev => {
                if (prev.some(item => item.id === newAnnouncement.id)) {
                  return prev;
                }
                return [newAnnouncement, ...prev];
              });
            } catch (e) {
              console.error("JSON parsing failed for WebSocket frame:", e);
            }
          }
        }
      };

      ws.onclose = () => {
        console.warn("WebSocket disconnect. Retrying reconnect in 5s...");
        reconnectTimeout = setTimeout(connectWebSocket, 5000);
      };

      ws.onerror = (err) => {
        console.error("WebSocket exception:", err);
        ws.close();
      };
    };

    connectWebSocket();

    return () => {
      if (ws) ws.close();
      clearTimeout(reconnectTimeout);
    };
  }, []);

  const filteredAnnouncements = useMemo(() => {
    return announcements.filter(item => {
      const matchesSearch = 
        item.sender.toLowerCase().includes(search.toLowerCase()) ||
        (item.originalMessage && item.originalMessage.toLowerCase().includes(search.toLowerCase())) ||
        item.tags.some(tag => tag.toLowerCase().includes(search.toLowerCase()));
      
      const matchesPlatform = platformFilter === 'ALL' || item.platform === platformFilter;
      const matchesImportance = importanceFilter === 'ALL' || item.importance === importanceFilter;
      
      return matchesSearch && matchesPlatform && matchesImportance;
    });
  }, [announcements, search, platformFilter, importanceFilter]);

  const stats = useMemo(() => {
    const total = announcements.length;
    const high = announcements.filter(a => a.importance === 'HIGH').length;
    const discordCount = announcements.filter(a => a.platform === 'DISCORD').length;
    const whatsappCount = announcements.filter(a => a.platform === 'WHATSAPP').length;
    
    const originalWordCount = announcements.reduce((sum, a) => sum + (a.originalMessage ? a.originalMessage.split(' ').length : 0), 0);
    const summaryWordCount = announcements.reduce((sum, a) => sum + (a.aiSummary ? a.aiSummary.join(' ').split(' ').length : 0), 0);
    const tokensSaved = Math.max(0, originalWordCount - summaryWordCount) * 4;

    return { total, high, discordCount, whatsappCount, tokensSaved };
  }, [announcements]);

  const handleSimulate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!simSender.trim() || !simMessage.trim()) return;

    setSimStep(1); // Webhook Step 1

    const payload = {
      sender: simSender,
      content: simMessage,
      conversationId: 'global-contest'
    };

    const endpoint = simPlatform === 'DISCORD' 
      ? `${API_BASE_URL}/api/v1/webhooks/discord` 
      : `${API_BASE_URL}/api/v1/webhooks/whatsapp`;

    fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Webhook-Token': 'tardis-secret-verify-token'
      },
      body: JSON.stringify(payload)
    })
    .then(res => {
      if (res.ok) {
        // Step 2: RabbitMQ queued (simulated delay transition)
        setSimStep(2);
        
        setTimeout(() => {
          setSimStep(3); // AI Processing (Qwen)
          
          setTimeout(() => {
            setSimStep(4); // WebSocket Broadcasting
            
            setTimeout(() => {
              setSimStep(0);
              setSimSender('');
              setSimMessage('');
            }, 1000);
          }, 1500);
        }, 1000);
      } else {
        alert("Failed to send webhook! Please check the Backend status (Spring Boot).");
        setSimStep(0);
      }
    })
    .catch(err => {
      console.warn("Backend offline. Performing local fallback simulation...", err);
      // Fallback local animation if backend is not running
      setSimStep(2);
      setTimeout(() => {
        setSimStep(3);
        setTimeout(() => {
          setSimStep(4);
          setTimeout(() => {
            const lines = simMessage.split(/[.!?]+/).map(s => s.trim()).filter(s => s.length > 5);
            const generatedSummary: string[] = [];
            
            if (lines.length > 0) {
              generatedSummary.push(`Main content: ${lines[0]}`);
            }
            if (lines.length > 1) {
              generatedSummary.push(`Detail: ${lines[1]}`);
            }
            if (simMessage.toLowerCase().includes('deadline') || simMessage.toLowerCase().includes('time') || simMessage.toLowerCase().includes('hour') || simMessage.toLowerCase().includes('limit')) {
              generatedSummary.push(`Note on specific timing or deadline requirements.`);
            }

            const tags = ['simulated'];
            if (simMessage.toLowerCase().includes('deadline')) tags.push('deadline');
            if (simMessage.toLowerCase().includes('code') || simMessage.toLowerCase().includes('api')) tags.push('technical');

            const newAnnouncement: Announcement = {
              id: Date.now().toString(),
              sender: simSender,
              platform: simPlatform,
              timestamp: 'Just now',
              originalMessage: simMessage,
              aiSummary: generatedSummary,
              tags: tags,
              importance: simImportance
            };

            setAnnouncements(prev => [newAnnouncement, ...prev]);
            setSimSender('');
            setSimMessage('');
            setSimStep(0);
          }, 600);
        }, 800);
      }, 700);
    });
  };

  return (
    <section className="dashboard-section section-gap">
      <div className="container">
        
        {/* Section Header */}
        <div className="dashboard-intro text-center">
          <span className="badge-live-pulse">REAL-TIME SIMULATION</span>
          <h2>Judges' Announcement Dashboard</h2>
          <p className="text-body text-muted">
            Experience real-time AI summarization. Use the simulation form below to send test announcements from Judges' Discord/WhatsApp and see it live.
          </p>
        </div>

        {/* Dashboard Stats */}
        <div className="stats-board-grid">
          <div className="card-default stat-board-card">
            <div className="stat-card-icon discord-bg"><MessageSquare size={20} /></div>
            <div className="stat-card-data">
              <h4>{stats.discordCount}</h4>
              <p className="text-small text-muted">Discord Announcements</p>
            </div>
          </div>
          <div className="card-default stat-board-card">
            <div className="stat-card-icon whatsapp-bg"><MessageSquare size={20} /></div>
            <div className="stat-card-data">
              <h4>{stats.whatsappCount}</h4>
              <p className="text-small text-muted">WhatsApp Announcements</p>
            </div>
          </div>
          <div className="card-default stat-board-card">
            <div className="stat-card-icon alert-bg"><ShieldAlert size={20} /></div>
            <div className="stat-card-data">
              <h4>{stats.high}</h4>
              <p className="text-small text-muted">Emergency (High)</p>
            </div>
          </div>
          <div className="card-default stat-board-card">
            <div className="stat-card-icon token-bg"><Sparkles size={20} /></div>
            <div className="stat-card-data">
              <h4>~{stats.tokensSaved}</h4>
              <p className="text-small text-muted">Words Saved</p>
            </div>
          </div>
        </div>

        <div className="dashboard-workspace-grid">
          
          {/* Left Panel: Filters & Live Feed */}
          <div className="feed-panel">
            <div className="feed-controls-box doodle-border">
              
              {/* Search Bar */}
              <div className="search-wrapper">
                <Search className="search-icon" size={18} />
                <input 
                  type="text" 
                  placeholder="Search by sender, content, or tag..." 
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  className="search-input"
                />
              </div>

              {/* Filtering Options */}
              <div className="filter-options-row">
                <div className="filter-group">
                  <span className="filter-label"><SlidersHorizontal size={14} /> Platform:</span>
                  <div className="filter-buttons">
                    <button 
                      className={`filter-btn ${platformFilter === 'ALL' ? 'active' : ''}`}
                      onClick={() => setPlatformFilter('ALL')}
                    >
                      All
                    </button>
                    <button 
                      className={`filter-btn ${platformFilter === 'DISCORD' ? 'active' : ''}`}
                      onClick={() => setPlatformFilter('DISCORD')}
                    >
                      Discord
                    </button>
                    <button 
                      className={`filter-btn ${platformFilter === 'WHATSAPP' ? 'active' : ''}`}
                      onClick={() => setPlatformFilter('WHATSAPP')}
                    >
                      WhatsApp
                    </button>
                  </div>
                </div>

                <div className="filter-group">
                  <span className="filter-label"><AlertCircle size={14} /> Importance:</span>
                  <div className="filter-buttons">
                    <button 
                      className={`filter-btn ${importanceFilter === 'ALL' ? 'active' : ''}`}
                      onClick={() => setImportanceFilter('ALL')}
                    >
                      All
                    </button>
                    <button 
                      className={`filter-btn ${importanceFilter === 'HIGH' ? 'active' : ''}`}
                      onClick={() => setImportanceFilter('HIGH')}
                    >
                      High
                    </button>
                    <button 
                      className={`filter-btn ${importanceFilter === 'MEDIUM' ? 'active' : ''}`}
                      onClick={() => setImportanceFilter('MEDIUM')}
                    >
                      Medium
                    </button>
                    <button 
                      className={`filter-btn ${importanceFilter === 'LOW' ? 'active' : ''}`}
                      onClick={() => setImportanceFilter('LOW')}
                    >
                      Low
                    </button>
                  </div>
                </div>

                <div className="filter-group">
                  <span className="filter-label"><Globe size={14} /> Timezone:</span>
                  <select
                    value={selectedTimezone}
                    onChange={(e) => setSelectedTimezone(e.target.value)}
                    className="timezone-select"
                  >
                    {UNIQUE_TIMEZONES.map(tz => (
                      <option key={tz.value} value={tz.value}>{tz.label}</option>
                    ))}
                  </select>
                </div>
              </div>
            </div>

            {/* Announcement Feed */}
            <div className="announcements-feed-list">
              {filteredAnnouncements.length === 0 ? (
                <div className="card-default empty-state-card text-center">
                  <p className="text-body text-muted">No announcements found matching the active filters.</p>
                </div>
              ) : (
                filteredAnnouncements.map((item) => (
                  <div 
                    key={item.id} 
                    className={`card-default announcement-card border-importance-${item.importance.toLowerCase()}`}
                  >
                    <div className="announcement-card-header">
                      <div className="sender-meta">
                        <span className={`platform-pill platform-color-${item.platform.toLowerCase()}`}>
                          {item.platform}
                        </span>
                        <h4 className="sender-title">{item.sender}</h4>
                      </div>
                      <div className="time-meta">
                        <span className="text-small text-muted">{formatTimestamp(item.timestamp, selectedTimezone)}</span>
                        <span className={`importance-tag level-${item.importance.toLowerCase()}`}>
                          {item.importance === 'HIGH' ? 'High' : item.importance === 'MEDIUM' ? 'Medium' : 'Low'}
                        </span>
                      </div>
                    </div>

                    {/* AI SUMMARY BOX */}
                    <div className="ai-summary-highlight doodle-border">
                      <div className="summary-title">
                        <Sparkles size={16} className="spark-icon" />
                        <span>AI Key Takeaways:</span>
                      </div>
                      <ul className="summary-list">
                        {item.aiSummary.map((bullet, idx) => (
                          <li key={idx} dangerouslySetInnerHTML={{ __html: bullet }} />
                        ))}
                      </ul>
                    </div>

                    {/* Original Message */}
                    <div className="original-message-section">
                      <button 
                        className="toggle-original-btn"
                        onClick={() => toggleExpand(item.id)}
                      >
                        {expandedCards[item.id] ? ' hide original message ▲' : ' view original message ▼'}
                      </button>
                      
                      {expandedCards[item.id] && (
                        <div className="original-msg-content doodle-border">
                          {item.originalMessage}
                        </div>
                      )}
                    </div>

                    <div className="announcement-card-footer">
                      <div className="tags-container">
                        {item.tags.map((tag) => (
                          <span key={tag} className="tag-pill">#{tag}</span>
                        ))}
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Right Panel: Announcement Simulator Form */}
          <div className="simulator-panel">
            <div className="card-default simulator-card">
              <div className="sim-header">
                <h3>Webhook Simulator</h3>
                <p className="text-small text-muted">Simulate sending raw announcements from Judges' communication channels.</p>
              </div>

              {simStep > 0 && (
                <div className="pipeline-loading-box doodle-border">
                  <div className="pipeline-steps">
                    <div className={`step-item ${simStep >= 1 ? 'active' : ''} ${simStep === 1 ? 'loading' : ''}`}>
                      <span className="step-num">{simStep > 1 ? '✓' : '1'}</span>
                      <span className="step-txt">Webhook Ingestion</span>
                    </div>
                    <div className={`step-item ${simStep >= 2 ? 'active' : ''} ${simStep === 2 ? 'loading' : ''}`}>
                      <span className="step-num">{simStep > 2 ? '✓' : '2'}</span>
                      <span className="step-txt">RabbitMQ Queue</span>
                    </div>
                    <div className={`step-item ${simStep >= 3 ? 'active' : ''} ${simStep === 3 ? 'loading' : ''}`}>
                      <span className="step-num">{simStep > 3 ? '✓' : '3'}</span>
                      <span className="step-txt">AI Processing</span>
                    </div>
                    <div className={`step-item ${simStep >= 4 ? 'active' : ''} ${simStep === 4 ? 'loading' : ''}`}>
                      <span className="step-num">{simStep > 4 ? '✓' : '4'}</span>
                      <span className="step-txt">Realtime Broadcast</span>
                    </div>
                  </div>
                  <div className="progress-bar-wrapper">
                    <div className="progress-bar-fill" style={{ width: `${(simStep / 4) * 100}%` }}></div>
                  </div>
                  <p className="pipeline-status-text">
                    {simStep === 1 && "Sending HTTP POST payload to Spring Boot Webhook API..."}
                    {simStep === 2 && "Spring Boot is queueing the message into RabbitMQ..."}
                    {simStep === 3 && "RabbitMQ Consumer triggered, calling Qwen LLM for summarization..."}
                    {simStep === 4 && "Saved to PostgreSQL, broadcasting via WebSocket stream to React UI..."}
                  </p>
                </div>
              )}

              <form onSubmit={handleSimulate} className="sim-form">
                <div className="form-group-flex">
                  <div className="form-field">
                    <label className="field-label">Platform Channel</label>
                    <select 
                      value={simPlatform}
                      onChange={(e) => setSimPlatform(e.target.value as 'DISCORD' | 'WHATSAPP')}
                      className="sim-select"
                      disabled={simStep > 0}
                    >
                      <option value="DISCORD">Discord Webhook</option>
                      <option value="WHATSAPP">WhatsApp Cloud API</option>
                    </select>
                  </div>
                  <div className="form-field">
                    <label className="field-label">Importance Level</label>
                    <select 
                      value={simImportance}
                      onChange={(e) => setSimImportance(e.target.value as 'HIGH' | 'MEDIUM' | 'LOW')}
                      className="sim-select"
                      disabled={simStep > 0}
                    >
                      <option value="HIGH">HIGH (Emergency)</option>
                      <option value="MEDIUM">MEDIUM (Normal)</option>
                      <option value="LOW">LOW (Low)</option>
                    </select>
                  </div>
                </div>

                <div className="form-field">
                  <label className="field-label">Judge Name (Sender)</label>
                  <input 
                    type="text" 
                    placeholder="e.g. Judge David, Chief Minh..." 
                    value={simSender}
                    onChange={(e) => setSimSender(e.target.value)}
                    className="sim-input"
                    disabled={simStep > 0}
                    required
                  />
                </div>

                <div className="form-field">
                  <label className="field-label">Original Announcement (Long Message)</label>
                  <textarea 
                    placeholder="Type a long official announcement from a judge..." 
                    value={simMessage}
                    onChange={(e) => setSimMessage(e.target.value)}
                    className="sim-textarea"
                    rows={5}
                    disabled={simStep > 0}
                    required
                  />
                </div>

                <button 
                  type="submit" 
                  className="btn btn-sm btn-primary sim-submit-btn" 
                  disabled={simStep > 0 || !simSender.trim() || !simMessage.trim()}
                >
                  <Send size={16} /> Send Webhook & Summarize
                </button>
              </form>
            </div>

            {/* Tech explanation info box */}
            <div className="card-default info-explain-card" style={{ marginTop: '16px' }}>
              <div className="info-header" style={{ display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '8px' }}>
                <BarChart2 size={18} style={{ color: 'var(--c-warning)' }} />
                <h4 style={{ margin: 0, fontSize: '14px' }}>Architecture Pattern</h4>
              </div>
              <p className="text-small" style={{ margin: 0, lineHeight: 1.5, fontSize: '12px' }}>
                In production, Spring Boot receives webhooks from Discord/WhatsApp, writes to PostgreSQL, queues them to RabbitMQ, and returns 200 OK in &lt; 50ms. An async background worker consumes the queue, calls Qwen LLM API for summarization, stores the summary, and broadcasts the live update to the React UI via WebSocket.
              </p>
            </div>
          </div>

        </div>
      </div>
    </section>
  );
}
