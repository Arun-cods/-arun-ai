import React, { useState, useRef, useEffect } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TextInput,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
  StatusBar,
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Alert
} from 'react-native';

const BACKEND_URL = 'http://10.133.8.198:8080';

export default function App() {
  const [theme, setTheme] = useState('dark');
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [model, setModel] = useState('gemini-3.6-flash');
  const [loading, setLoading] = useState(false);
  const scrollViewRef = useRef();

  const isDark = theme === 'dark';
  const colors = isDark ? {
    bg: '#131314',
    surface: '#1e1f20',
    elevated: '#282a2c',
    text: '#f1f3f4',
    textMuted: '#9aa0a6',
    border: '#2d3034',
    accent: '#8ab4f8',
    userBubble: '#282a2c',
    botBubble: '#1e1f20',
  } : {
    bg: '#f8f9fa',
    surface: '#ffffff',
    elevated: '#f1f3f4',
    text: '#1f1f1f',
    textMuted: '#5f6368',
    border: '#e3e3e3',
    accent: '#1a73e8',
    userBubble: '#e8f0fe',
    botBubble: '#ffffff',
  };

  const suggestions = [
    { title: '💡 Brainstorm', desc: 'Create 3 viral AI product concepts' },
    { title: '💻 Write Code', desc: 'Write a Java Spring Boot REST API' },
    { title: '📝 Draft Email', desc: 'Draft a polite follow-up business email' },
    { title: '🔍 Explain', desc: 'Explain Quantum Computing simply' },
  ];

  const sendMessage = async (textToSend) => {
    const query = textToSend || input.trim();
    if (!query || loading) return;

    const newMessages = [...messages, { role: 'user', content: query }];
    setMessages(newMessages);
    setInput('');
    setLoading(true);

    try {
      const response = await fetch(`${BACKEND_URL}/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          message: query,
          history: newMessages.slice(0, -1).map(m => ({ role: m.role, content: m.content })),
          model: model
        })
      });

      const data = await response.json();
      setMessages([...newMessages, { role: 'assistant', content: data.message || 'No response.' }]);
    } catch (err) {
      setMessages([...newMessages, { role: 'assistant', content: 'Could not connect to Arun AI backend. Please verify your connection.' }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
      <StatusBar barStyle={isDark ? 'light-content' : 'dark-content'} />
      
      {/* Top Header */}
      <View style={[styles.header, { backgroundColor: colors.surface, borderBottomColor: colors.border }]}>
        <View style={styles.brand}>
          <Text style={styles.sparkle}>✦</Text>
          <Text style={[styles.brandText, { color: colors.text }]}>Arun AI</Text>
          <View style={styles.badge}>
            <Text style={styles.badgeText}>3.6</Text>
          </View>
        </View>

        <View style={styles.headerActions}>
          <TouchableOpacity 
            onPress={() => setTheme(isDark ? 'light' : 'dark')} 
            style={[styles.iconButton, { backgroundColor: colors.elevated }]}>
            <Text style={{ fontSize: 16 }}>{isDark ? '☀️' : '🌙'}</Text>
          </TouchableOpacity>
          <TouchableOpacity 
            onPress={() => setMessages([])} 
            style={[styles.iconButton, { backgroundColor: colors.elevated }]}>
            <Text style={{ fontSize: 16 }}>🗑️</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Model Selector Pill */}
      <View style={styles.modelBar}>
        <TouchableOpacity 
          style={[styles.modelPill, { backgroundColor: colors.surface, borderColor: colors.border }]}
          onPress={() => Alert.alert('Model', 'Currently using Gemini 3.6 Flash for lightning speed.')}>
          <Text style={[styles.modelText, { color: colors.accent }]}>✦ Gemini 3.6 Flash ▾</Text>
        </TouchableOpacity>
      </View>

      {/* Chat Messages */}
      <ScrollView
        ref={scrollViewRef}
        style={styles.chatArea}
        contentContainerStyle={styles.chatContent}
        onContentSizeChange={() => scrollViewRef.current?.scrollToEnd({ animated: true })}>
        
        {messages.length === 0 ? (
          <View style={styles.welcomeHero}>
            <Text style={styles.heroSparkle}>✦</Text>
            <Text style={[styles.heroTitle, { color: colors.text }]}>Hello, Arun</Text>
            <Text style={[styles.heroSubtitle, { color: colors.textMuted }]}>How can I help you today?</Text>

            <View style={styles.suggestionGrid}>
              {suggestions.map((item, idx) => (
                <TouchableOpacity 
                  key={idx} 
                  style={[styles.suggestionCard, { backgroundColor: colors.surface, borderColor: colors.border }]}
                  onPress={() => sendMessage(item.desc)}>
                  <Text style={[styles.suggestionTitle, { color: colors.accent }]}>{item.title}</Text>
                  <Text style={[styles.suggestionDesc, { color: colors.textMuted }]}>{item.desc}</Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>
        ) : (
          messages.map((msg, idx) => (
            <View key={idx} style={[styles.msgRow, msg.role === 'user' ? styles.userRow : styles.botRow]}>
              <View style={[
                styles.bubble, 
                msg.role === 'user' 
                  ? [styles.userBubble, { backgroundColor: colors.userBubble }] 
                  : [styles.botBubble, { backgroundColor: colors.botBubble, borderColor: colors.border }]
              ]}>
                <Text style={[styles.msgText, { color: colors.text }]}>{msg.content}</Text>
              </View>
            </View>
          ))
        )}

        {loading && (
          <View style={[styles.msgRow, styles.botRow]}>
            <View style={[styles.bubble, styles.botBubble, { backgroundColor: colors.botBubble, borderColor: colors.border }]}>
              <ActivityIndicator color={colors.accent} size="small" />
            </View>
          </View>
        )}
      </ScrollView>

      {/* Composer Input Bar */}
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
        <View style={[styles.composerWrap, { backgroundColor: colors.bg }]}>
          <View style={[styles.composer, { backgroundColor: colors.surface, borderColor: colors.border }]}>
            <TextInput
              style={[styles.input, { color: colors.text }]}
              placeholder="Ask Arun AI anything..."
              placeholderTextColor={colors.textMuted}
              value={input}
              onChangeText={setInput}
              multiline
            />
            <TouchableOpacity 
              style={[styles.sendBtn, { backgroundColor: input.trim() ? colors.accent : colors.elevated }]}
              onPress={() => sendMessage()}
              disabled={!input.trim() || loading}>
              <Text style={{ color: input.trim() ? '#fff' : colors.textMuted, fontWeight: 'bold' }}>↑</Text>
            </TouchableOpacity>
          </View>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
  },
  brand: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  sparkle: { fontSize: 20, color: '#3b82f6' },
  brandText: { fontSize: 18, fontWeight: '700' },
  badge: { backgroundColor: '#3b82f6', borderRadius: 6, paddingHorizontal: 6, paddingVertical: 2 },
  badgeText: { color: '#fff', fontSize: 10, fontWeight: 'bold' },
  headerActions: { flexDirection: 'row', gap: 8 },
  iconButton: { width: 36, height: 36, borderRadius: 18, alignItems: 'center', justifyContent: 'center' },
  modelBar: { paddingHorizontal: 16, paddingVertical: 8 },
  modelPill: { alignSelf: 'flex-start', paddingHorizontal: 12, paddingVertical: 6, borderRadius: 16, borderWidth: 1 },
  modelText: { fontSize: 12, fontWeight: '600' },
  chatArea: { flex: 1 },
  chatContent: { padding: 16 },
  welcomeHero: { alignItems: 'center', marginTop: 40 },
  heroSparkle: { fontSize: 42, color: '#3b82f6', marginBottom: 10 },
  heroTitle: { fontSize: 28, fontWeight: '800', marginBottom: 6 },
  heroSubtitle: { fontSize: 15, marginBottom: 28 },
  suggestionGrid: { width: '100%', gap: 10 },
  suggestionCard: { padding: 14, borderRadius: 14, borderWidth: 1 },
  suggestionTitle: { fontSize: 14, fontWeight: '700', marginBottom: 4 },
  suggestionDesc: { fontSize: 12 },
  msgRow: { marginVertical: 6, flexDirection: 'row' },
  userRow: { justifyContent: 'flex-end' },
  botRow: { justifyContent: 'flex-start' },
  bubble: { maxWidth: '85%', padding: 14, borderRadius: 18 },
  userBubble: { borderBottomRightRadius: 4 },
  botBubble: { borderBottomLeftRadius: 4, borderWidth: 1 },
  msgText: { fontSize: 15, lineHeight: 22 },
  composerWrap: { paddingHorizontal: 16, paddingVertical: 10 },
  composer: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 24,
    borderWidth: 1,
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  input: { flex: 1, fontSize: 15, maxHeight: 100 },
  sendBtn: { width: 34, height: 34, borderRadius: 17, alignItems: 'center', justifyContent: 'center', marginLeft: 8 },
});