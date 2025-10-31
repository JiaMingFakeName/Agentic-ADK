# Copyright (C) 2025 AIDC-AI
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

"""
Customer Service Chatbot

A production-ready customer service agent that handles:
- Order inquiries
- Technical support
- Account management
- Escalation to human agents
"""

from typing import Dict, Any, List, Optional
from dataclasses import dataclass
from enum import Enum
from datetime import datetime


class IntentType(Enum):
    """Customer intent categories"""
    ORDER_STATUS = "order_status"
    TECHNICAL_SUPPORT = "technical_support"
    ACCOUNT_MANAGEMENT = "account_management"
    PRODUCT_INQUIRY = "product_inquiry"
    COMPLAINT = "complaint"
    GENERAL_INQUIRY = "general_inquiry"


class Priority(Enum):
    """Issue priority levels"""
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    URGENT = "urgent"


@dataclass
class CustomerContext:
    """Customer conversation context"""
    customer_id: str
    conversation_history: List[Dict[str, str]]
    detected_intent: Optional[IntentType] = None
    priority: Priority = Priority.MEDIUM
    requires_escalation: bool = False
    sentiment: str = "neutral"  # positive, neutral, negative


class IntentClassifier:
    """Classifies customer intent from message"""
    
    def classify(self, message: str) -> IntentType:
        """Determine customer intent"""
        message_lower = message.lower()
        
        # Order-related keywords
        if any(word in message_lower for word in ['order', 'delivery', 'shipping', 'tracking']):
            return IntentType.ORDER_STATUS
        
        # Technical support keywords
        elif any(word in message_lower for word in ['error', 'bug', 'not working', 'broken', 'crash']):
            return IntentType.TECHNICAL_SUPPORT
        
        # Account keywords
        elif any(word in message_lower for word in ['account', 'password', 'login', 'settings']):
            return IntentType.ACCOUNT_MANAGEMENT
        
        # Product inquiry keywords
        elif any(word in message_lower for word in ['product', 'price', 'feature', 'available']):
            return IntentType.PRODUCT_INQUIRY
        
        # Complaint keywords
        elif any(word in message_lower for word in ['complaint', 'unsatisfied', 'disappointed', 'refund']):
            return IntentType.COMPLAINT
        
        else:
            return IntentType.GENERAL_INQUIRY


class SentimentAnalyzer:
    """Analyzes customer sentiment"""
    
    def analyze(self, message: str) -> str:
        """Determine message sentiment"""
        message_lower = message.lower()
        
        positive_words = ['great', 'good', 'excellent', 'happy', 'satisfied', 'thanks', 'love']
        negative_words = ['bad', 'terrible', 'awful', 'angry', 'frustrated', 'disappointed', 'hate']
        
        pos_count = sum(1 for word in positive_words if word in message_lower)
        neg_count = sum(1 for word in negative_words if word in message_lower)
        
        if neg_count > pos_count:
            return "negative"
        elif pos_count > neg_count:
            return "positive"
        else:
            return "neutral"


class OrderService:
    """Handles order-related queries"""
    
    def __init__(self):
        # Simulated order database
        self.orders = {
            "ORD-001": {
                "status": "shipped",
                "tracking": "TRK-123456",
                "expected_delivery": "2025-10-25"
            },
            "ORD-002": {
                "status": "processing",
                "tracking": None,
                "expected_delivery": "2025-10-28"
            }
        }
    
    def get_order_status(self, order_id: str) -> str:
        """Get order status"""
        order = self.orders.get(order_id)
        
        if not order:
            return f"I couldn't find an order with ID {order_id}. Please check the order number and try again."
        
        response = f"Your order {order_id} is currently {order['status']}."
        
        if order['tracking']:
            response += f" Tracking number: {order['tracking']}."
        
        if order['expected_delivery']:
            response += f" Expected delivery: {order['expected_delivery']}."
        
        return response


class TechnicalSupportService:
    """Handles technical support queries"""
    
    def __init__(self):
        self.knowledge_base = {
            "login": "To reset your password, click 'Forgot Password' on the login page and follow the instructions sent to your email.",
            "error": "Please try clearing your browser cache and cookies. If the issue persists, contact our support team.",
            "slow": "Try closing other applications and restarting your browser. Also ensure you have a stable internet connection."
        }
    
    def get_solution(self, issue: str) -> str:
        """Get technical solution"""
        issue_lower = issue.lower()
        
        for keyword, solution in self.knowledge_base.items():
            if keyword in issue_lower:
                return solution
        
        return "I'll create a support ticket for our technical team to investigate this issue. They'll contact you within 24 hours."


class CustomerServiceBot:
    """Main customer service chatbot"""
    
    def __init__(self):
        self.intent_classifier = IntentClassifier()
        self.sentiment_analyzer = SentimentAnalyzer()
        self.order_service = OrderService()
        self.technical_support = TechnicalSupportService()
        self.contexts: Dict[str, CustomerContext] = {}
    
    def process_message(self, customer_id: str, message: str) -> str:
        """Process customer message and generate response"""
        
        # Get or create customer context
        if customer_id not in self.contexts:
            self.contexts[customer_id] = CustomerContext(
                customer_id=customer_id,
                conversation_history=[]
            )
        
        context = self.contexts[customer_id]
        
        # Add message to history
        context.conversation_history.append({
            "role": "customer",
            "message": message,
            "timestamp": datetime.now().isoformat()
        })
        
        # Classify intent
        intent = self.intent_classifier.classify(message)
        context.detected_intent = intent
        
        # Analyze sentiment
        sentiment = self.sentiment_analyzer.analyze(message)
        context.sentiment = sentiment
        
        # Adjust priority based on sentiment and intent
        if sentiment == "negative" or intent == IntentType.COMPLAINT:
            context.priority = Priority.HIGH
        
        print(f"\n[Bot Analysis]")
        print(f"  Intent: {intent.value}")
        print(f"  Sentiment: {sentiment}")
        print(f"  Priority: {context.priority.value}\n")
        
        # Route to appropriate handler
        response = self._route_to_handler(intent, message, context)
        
        # Add response to history
        context.conversation_history.append({
            "role": "bot",
            "message": response,
            "timestamp": datetime.now().isoformat()
        })
        
        return response
    
    def _route_to_handler(self, intent: IntentType, message: str, 
                          context: CustomerContext) -> str:
        """Route to appropriate service handler"""
        
        if intent == IntentType.ORDER_STATUS:
            # Extract order ID (simplified)
            words = message.split()
            order_id = None
            for word in words:
                if word.startswith("ORD-"):
                    order_id = word
                    break
            
            if order_id:
                return self.order_service.get_order_status(order_id)
            else:
                return "I'd be happy to help you track your order! Could you please provide your order number? It starts with 'ORD-'."
        
        elif intent == IntentType.TECHNICAL_SUPPORT:
            response = self.technical_support.get_solution(message)
            
            # Check if escalation needed
            if "support ticket" in response:
                context.requires_escalation = True
            
            return response
        
        elif intent == IntentType.COMPLAINT:
            context.requires_escalation = True
            return "I sincerely apologize for your experience. I'm escalating your concern to our customer care team. A senior representative will contact you within 2 hours to resolve this issue."
        
        elif intent == IntentType.ACCOUNT_MANAGEMENT:
            return "For account security reasons, please visit your account settings at www.example.com/account or contact our support team at support@example.com for assistance."
        
        elif intent == IntentType.PRODUCT_INQUIRY:
            return "I can help you with product information! Our full catalog is available at www.example.com/products. Is there a specific product you're interested in?"
        
        else:
            return "Thank you for contacting us! How can I assist you today? I can help with orders, technical issues, account questions, or product information."


def demo_customer_service_bot():
    """Demonstrate customer service chatbot"""
    
    print("="*60)
    print("Customer Service Chatbot Demo")
    print("="*60)
    
    bot = CustomerServiceBot()
    customer_id = "CUST-12345"
    
    # Scenario 1: Order inquiry
    print("\n" + "="*60)
    print("Scenario 1: Order Status Inquiry")
    print("="*60)
    
    message = "Hi, I want to check my order ORD-001 status"
    print(f"\nCustomer: {message}")
    response = bot.process_message(customer_id, message)
    print(f"Bot: {response}")
    
    # Scenario 2: Technical support
    print("\n" + "="*60)
    print("Scenario 2: Technical Support")
    print("="*60)
    
    customer_id = "CUST-67890"
    message = "I'm getting an error when trying to login, it says invalid credentials"
    print(f"\nCustomer: {message}")
    response = bot.process_message(customer_id, message)
    print(f"Bot: {response}")
    
    # Scenario 3: Complaint (high priority)
    print("\n" + "="*60)
    print("Scenario 3: Customer Complaint (High Priority)")
    print("="*60)
    
    customer_id = "CUST-11111"
    message = "This is terrible! My order arrived damaged and I'm very disappointed"
    print(f"\nCustomer: {message}")
    response = bot.process_message(customer_id, message)
    print(f"Bot: {response}")
    
    # Check if escalation was triggered
    context = bot.contexts[customer_id]
    if context.requires_escalation:
        print("\n  [System] Escalation triggered - Routing to human agent")
    
    # Scenario 4: Multi-turn conversation
    print("\n" + "="*60)
    print("Scenario 4: Multi-Turn Conversation")
    print("="*60)
    
    customer_id = "CUST-22222"
    
    messages = [
        "Hello, I need help",
        "I want to know about your products",
        "Do you have wireless headphones?",
        "What's the price?"
    ]
    
    for msg in messages:
        print(f"\nCustomer: {msg}")
        response = bot.process_message(customer_id, msg)
        print(f"Bot: {response}")
    
    # Show conversation summary
    print("\n" + "="*60)
    print("Conversation Summary")
    print("="*60)
    
    context = bot.contexts[customer_id]
    print(f"\nCustomer ID: {context.customer_id}")
    print(f"Total Messages: {len(context.conversation_history)}")
    print(f"Final Intent: {context.detected_intent.value if context.detected_intent else 'N/A'}")
    print(f"Final Sentiment: {context.sentiment}")
    print(f"Priority: {context.priority.value}")
    print(f"Escalation Required: {context.requires_escalation}")


if __name__ == "__main__":
    demo_customer_service_bot()
    
    print(f"\n\n{'='*60}")
    print("Customer Service Bot demonstration completed!")
    print(f"{'='*60}")

